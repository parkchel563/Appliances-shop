package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.appliances.model.Client;
import com.project.appliances.model.Employee;
import com.project.appliances.model.User;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.service.impl.UserServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "user@test.com";

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    // findUserByEmail()
    @Test
    void shouldFindClientByEmail() {
        Client client = new Client();
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        Optional<User> result = userService.findUserByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(client, result.get());
        verify(employeeRepository, never()).findByEmail(EMAIL);
    }

    @Test
    void shouldFindEmployeeByEmail_whenClientNotFound() {
        Employee employee = new Employee();

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        Optional<User> result = userService.findUserByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(employee, result.get());

        verify(clientRepository).findByEmail(EMAIL);
        verify(employeeRepository).findByEmail(EMAIL);
    }

    @Test
    void shouldReturnEmpty_whenUserNotFound() {
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserByEmail(EMAIL);

        assertTrue(result.isEmpty());
    }

    // registerFailedAttempt() && update()
    @Test
    void shouldIncrementFailedAttempts_whenAttemptsAreNull_andSaveClient() {
        Client client = new Client();
        client.setFailedAttempts(null);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        userService.registerFailedAttempt(EMAIL);

        assertEquals(1, client.getFailedAttempts());
        verify(clientRepository).save(client);
    }

    @Test
    void shouldIncrementFailedAttempts_whenAttemptsAreNotNull() {
        Client client = new Client();
        client.setFailedAttempts(1);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        userService.registerFailedAttempt(EMAIL);

        assertEquals(2, client.getFailedAttempts());
        verify(clientRepository).save(client);
    }

    @Test
    void shouldLockAccount_whenMaxAttemptsReached_andSaveEmployee() {
        Employee employee = new Employee();
        employee.setFailedAttempts(2);

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        userService.registerFailedAttempt(EMAIL);

        assertEquals(3, employee.getFailedAttempts());
        assertFalse(employee.getAccountNonLocked());
        assertNotNull(employee.getLockTime());

        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldDoNothing_whenUserNotFoundOnFailedAttempt() {
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        userService.registerFailedAttempt(EMAIL);

        verify(clientRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    // resetFailedAttempts()
    @Test
    void shouldResetFailedAttemptsSuccessfully() {
        Client client = new Client();
        client.setFailedAttempts(5);
        client.setAccountNonLocked(false);
        client.setLockTime(LocalDateTime.now());

        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        userService.resetFailedAttempts(EMAIL);

        assertEquals(0, client.getFailedAttempts());
        assertTrue(client.getAccountNonLocked());
        assertNull(client.getLockTime());
        verify(clientRepository).save(client);
    }

    @Test
    void shouldDoNothing_whenUserNotFoundOnResetAttempts() {
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        userService.resetFailedAttempts(EMAIL);

        verify(clientRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    // unlockIfLockExpired()
    @Test
    void shouldReturnEarly_whenAccountAlreadyNonLocked() {
        Client client = new Client();
        client.setAccountNonLocked(true);

        userService.unlockIfLockExpired(client);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldDoNothing_whenLockTimeIsNull() {
        Client client = new Client();
        client.setAccountNonLocked(false);
        client.setLockTime(null);

        userService.unlockIfLockExpired(client);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldDoNothing_whenLockNotExpired() {
        Client client = new Client();
        client.setAccountNonLocked(false);
        client.setLockTime(LocalDateTime.now().minusMinutes(5));

        userService.unlockIfLockExpired(client);

        assertFalse(client.getAccountNonLocked());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldUnlockAccount_whenLockExpired() {
        Client client = new Client();
        client.setAccountNonLocked(false);
        client.setFailedAttempts(3);
        client.setLockTime(LocalDateTime.now().minusMinutes(20));

        userService.unlockIfLockExpired(client);

        assertTrue(client.getAccountNonLocked());
        assertEquals(0, client.getFailedAttempts());
        assertNull(client.getLockTime());
        verify(clientRepository).save(client);
    }
}

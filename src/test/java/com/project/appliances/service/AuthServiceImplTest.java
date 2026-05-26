package com.project.appliances.service;


import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.mapper.ClientMapper;
import com.project.appliances.model.Client;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private static final String EMAIL = "newuser@test.com";

    @Test
    void shouldRegisterClientSuccessfully() {
        ClientRegistrationDto dto = new ClientRegistrationDto();
        dto.setEmail(EMAIL);
        dto.setPassword("password");

        Client client = new Client();
        String encodedPassword = "encodedPassword";

        when(clientRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(employeeRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(clientMapper.toEntity(dto)).thenReturn(client);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn(encodedPassword);

        authServiceImpl.registerClient(dto);

        assertEquals(encodedPassword, client.getPassword());
        verify(clientRepository).save(client);
    }

    @Test
    void shouldThrowException_whenEmailExistsInClientRepository() {
        ClientRegistrationDto dto = new ClientRegistrationDto();
        dto.setEmail(EMAIL);

        when(clientRepository.existsByEmail(EMAIL)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authServiceImpl.registerClient(dto));

        assertEquals("Email already in use", ex.getMessage());

        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenEmailExistsInEmployeeRepository() {
        ClientRegistrationDto dto = new ClientRegistrationDto();
        dto.setEmail(EMAIL);

        when(employeeRepository.existsByEmail(EMAIL)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authServiceImpl.registerClient(dto));

        assertEquals("Email already in use", ex.getMessage());

        verify(employeeRepository, never()).save(any());
    }
}

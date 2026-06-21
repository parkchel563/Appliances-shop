package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.employee.EmployeeCreateDto;
import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.exception.EmployeeNotFoundException;
import com.project.appliances.mapper.EmployeeMapper;
import com.project.appliances.model.Employee;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.impl.EmployeeServiceImpl;
import com.project.appliances.util.PasswordGenerator;
import com.project.appliances.util.TestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    private static final String TARGET_EMAIL = "target@employee.com";
    private static final String CURRENT_USER_EMAIL = "admin@employee.com";
    private static final Long EMPLOYEE_ID = 1L;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private OrdersRepository ordersRepository;
    @InjectMocks
    private EmployeeServiceImpl employeeServiceImpl;

    @Test
    void shouldReturnAllEmployees() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeDto dto = new EmployeeDto();

        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toDto(employee)).thenReturn(dto);

        List<EmployeeDto> result = employeeServiceImpl.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.getFirst());
    }

    @Test
    void shouldReturnPagedEmployees() {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeDto dto = new EmployeeDto();
        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(employeeMapper.toDto(employee)).thenReturn(dto);

        Page<EmployeeDto> result = employeeServiceImpl.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmployeeProfile() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeUpdateProfileDto dto = TestDataFactory.createEmployeeUpdateDto(TARGET_EMAIL);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(employeeMapper.toUpdateProfileDto(employee)).thenReturn(dto);

        EmployeeUpdateProfileDto result = employeeServiceImpl.getEmployeeProfile(EMPLOYEE_ID);

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowException_whenEmployeeNotFoundById() {
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeServiceImpl.getEmployeeProfile(EMPLOYEE_ID));
    }

    @Test
    void shouldUpdateEmployeeProfileSuccessfully() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeUpdateProfileDto dto = TestDataFactory.createEmployeeUpdateDto("new@gmail.com");

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(clientRepository.existsByEmail("new@gmail.com")).thenReturn(false);

        employeeServiceImpl.updateEmployeeProfile(EMPLOYEE_ID, dto);

        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldThrowException_whenUpdatingAndEmailExistsInEmployeeRepository() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeUpdateProfileDto dto = TestDataFactory.createEmployeeUpdateDto("new@gmail.com");

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("new@gmail.com")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                                                () -> employeeServiceImpl.updateEmployeeProfile(EMPLOYEE_ID, dto));

        assertEquals("Email already exists", ex.getMessage());

        verify(employeeRepository, never()).save(employee);
    }

    @Test
    void shouldThrowException_whenUpdatingAndEmailExistsInClientRepo() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeUpdateProfileDto dto = TestDataFactory.createEmployeeUpdateDto("new@mail.com");

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(clientRepository.existsByEmail("new@mail.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> employeeServiceImpl.updateEmployeeProfile(EMPLOYEE_ID, dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenEmployeeNotFoundOnUpdate() {
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class,
                     () -> employeeServiceImpl.updateEmployeeProfile(EMPLOYEE_ID, new EmployeeUpdateProfileDto()));
    }

    @Test
    void shouldUpdateEmployeeProfile_whenEmailIsNotChanged() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        EmployeeUpdateProfileDto dto = TestDataFactory.createEmployeeUpdateDto(TARGET_EMAIL);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

        employeeServiceImpl.updateEmployeeProfile(EMPLOYEE_ID, dto);

        verify(employeeRepository, never()).existsByEmail(anyString());
        verify(clientRepository, never()).existsByEmail(anyString());
        verify(employeeMapper).toUpdateEntity(dto, employee);
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldGenerateAndSaveNewPassword() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);
        String rawPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(passwordGenerator.generatePassword(10)).thenReturn(rawPassword);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = employeeServiceImpl.generatePassword(EMPLOYEE_ID);

        assertEquals(rawPassword, result);
        assertEquals(encodedPassword, employee.getPassword());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldThrowException_whenEmployeeNotFoundOnGeneratePassword() {
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeServiceImpl.generatePassword(EMPLOYEE_ID));
    }

    @Test
    void shouldDeleteEmployeeProfile_andReturnTrue_whenDeletingSelf() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, CURRENT_USER_EMAIL);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(ordersRepository.existsByEmployeeIdAndStatusIn(eq(EMPLOYEE_ID), anyList())).thenReturn(false);

        boolean deleteSelf = employeeServiceImpl.deleteEmployeeProfile(EMPLOYEE_ID, CURRENT_USER_EMAIL);

        assertTrue(deleteSelf);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void shouldDeleteEmployeeProfile_andReturnFalse_whenDeletingAnotherEmployee() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(ordersRepository.existsByEmployeeIdAndStatusIn(eq(EMPLOYEE_ID), anyList())).thenReturn(false);

        boolean deleteSelf = employeeServiceImpl.deleteEmployeeProfile(EMPLOYEE_ID, CURRENT_USER_EMAIL);

        assertFalse(deleteSelf);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void shouldThrowException_whenDeletingEmployeeWithActiveOrders() {
        Employee employee = TestDataFactory.createEmployee(EMPLOYEE_ID, TARGET_EMAIL);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(ordersRepository.existsByEmployeeIdAndStatusIn(eq(EMPLOYEE_ID), anyList())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                     () -> employeeServiceImpl.deleteEmployeeProfile(EMPLOYEE_ID, CURRENT_USER_EMAIL));
        verify(employeeRepository, never()).delete(employee);
    }

    @Test
    void shouldThrowException_whenEmployeeNotFoundOnDelete() {
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class,
                     () -> employeeServiceImpl.deleteEmployeeProfile(EMPLOYEE_ID, CURRENT_USER_EMAIL));
    }

    @Test
    void shouldCreateEmployeeSuccessfully() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setEmail(TARGET_EMAIL);

        Employee employee = new Employee();
        String rawPassword = "generatedPassword";
        String encodedPassword = "encodedPassword";

        when(clientRepository.existsByEmail(TARGET_EMAIL)).thenReturn(false);
        when(employeeRepository.existsByEmail(TARGET_EMAIL)).thenReturn(false);
        when(passwordGenerator.generatePassword(10)).thenReturn(rawPassword);
        when(employeeMapper.createToEntity(dto)).thenReturn(employee);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = employeeServiceImpl.createEmployee(dto);

        assertEquals(rawPassword, result);
        assertEquals(encodedPassword, employee.getPassword());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldThrowException_whenCreatingEmployeeAndEmailExistsInClientRepository() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setEmail(TARGET_EMAIL);

        when(clientRepository.existsByEmail(TARGET_EMAIL)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> employeeServiceImpl.createEmployee(dto));

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreatingEmployeeAndEmailExistsInEmployeeRepository() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setEmail(TARGET_EMAIL);

        when(clientRepository.existsByEmail(TARGET_EMAIL)).thenReturn(false);
        when(employeeRepository.existsByEmail(TARGET_EMAIL)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> employeeServiceImpl.createEmployee(dto));

        verify(employeeRepository, never()).save(any());
    }
}

package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.exception.ClientNotFoundException;
import com.project.appliances.mapper.ClientMapper;
import com.project.appliances.model.Client;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.service.impl.ClientServiceImpl;
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
class ClientServiceImplTest {

    private static final String CURRENT_EMAIL = "old@user.com";
    private static final String NEW_EMAIL = "new@user.com";
    private static final Long CLIENT_ID = 1L;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private ClientServiceImpl clientService;

    // getProfileForEdit()

    @Test
    void shouldReturnProfileForEdit() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(CURRENT_EMAIL);

        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientMapper.toUpdateProfileDto(client)).thenReturn(dto);

        ClientUpdateProfileDto result = clientService.getProfileForEdit(CURRENT_EMAIL);

        assertEquals(dto, result);
        verify(clientRepository).findByEmail(CURRENT_EMAIL);
    }

    @Test
    void shouldThrowExceptionWhenProfileNotFoundByEmail() {
        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.getProfileForEdit(CURRENT_EMAIL));
    }

    // updateProfile()
    @Test
    void shouldUpdateProfile_whenEmailIsUnchanged() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(CURRENT_EMAIL);

        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.of(client));

        boolean emailChanged = clientService.updateProfile(CURRENT_EMAIL, dto);

        assertFalse(emailChanged);
        verify(clientRepository, never()).existsByEmail(any());
        verify(clientMapper).updateEntity(dto, client);
        verify(clientRepository).save(client);
    }

    @Test
    void shouldUpdateProfile_whenEmailChangedAndAvailable() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(NEW_EMAIL);

        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        boolean emailChanged = clientService.updateProfile(CURRENT_EMAIL, dto);

        assertTrue(emailChanged);
        verify(clientMapper).updateEntity(dto, client);
        verify(clientRepository).save(client);
    }

    @Test
    void shouldThrowException_whenUpdatingProfileAndNewEmailAlreadyExists() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(NEW_EMAIL);

        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> clientService.updateProfile(CURRENT_EMAIL, dto));

        assertEquals("validation.email.exist", ex.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenClientNotFoundOnUpdateProfile() {
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(NEW_EMAIL);
        when(clientRepository.findByEmail(CURRENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.updateProfile(CURRENT_EMAIL, dto));
    }

    // updateClientProfile()

    @Test
    void shouldUpdateClientProfileById_whenEmailUnchanged() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(CURRENT_EMAIL);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.updateClientProfile(CLIENT_ID, dto);

        verify(clientRepository, never()).existsByEmail(any());
        verify(clientMapper).updateEntity(dto, client);
        verify(clientRepository).save(client);
    }

    @Test
    void shouldUpdateClientProfileById_whenEmailChangedAndAvailable() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(NEW_EMAIL);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        clientService.updateClientProfile(CLIENT_ID, dto);

        verify(clientRepository).save(client);
    }

    @Test
    void shouldThrowException_whenUpdatingClientProfileByIdAndEmailExists() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = TestDataFactory.createClientUpdateDto(NEW_EMAIL);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> clientService.updateClientProfile(CLIENT_ID, dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenClientNotFoundOnUpdateProfileById() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class,
                     () -> clientService.updateClientProfile(CLIENT_ID, new ClientUpdateProfileDto()));
    }

    // getClientProfile()

    @Test
    void shouldGetClientProfileById() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientUpdateProfileDto dto = new ClientUpdateProfileDto();

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientMapper.toUpdateProfileDto(client)).thenReturn(dto);

        ClientUpdateProfileDto result = clientService.getClientProfile(CLIENT_ID);

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowException_whenClientNotFoundById() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> clientService.getClientProfile(CLIENT_ID));
    }

    // getAll() & findAll() (Pagination)

    @Test
    void shouldReturnAllClients() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientDto dto = new ClientDto();

        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(clientMapper.toDto(client)).thenReturn(dto);

        List<ClientDto> result = clientService.getAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void shouldReturnPagedClients() {
        ClientSearchCriteria criteria = new ClientSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        ClientDto dto = new ClientDto();
        Page<Client> page = new PageImpl<>(List.of(client));

        when(clientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(clientMapper.toDto(client)).thenReturn(dto);

        Page<ClientDto> result = clientService.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findAll(any(Specification.class), eq(pageable));
    }

    // deleteClientProfile()

    @Test
    void shouldDeleteClientProfileSuccessfully() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.deleteClientProfile(CLIENT_ID);

        verify(clientRepository).delete(client);
    }
    
    @Test
    void shouldThrowException_whenClientNotFoundOnDelete() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> clientService.deleteClientProfile(CLIENT_ID));
    }

    // generatePassword()

    @Test
    void shouldGenerateAndSaveNewPassword() {
        Client client = TestDataFactory.createClient(CLIENT_ID, CURRENT_EMAIL);
        String rawPassword = "securePassword123";
        String encodedPassword = "encodedPassword";

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(passwordGenerator.generatePassword(10)).thenReturn(rawPassword);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = clientService.generatePassword(CLIENT_ID);

        assertEquals(rawPassword, result);
        assertEquals(encodedPassword, client.getPassword());
        verify(clientRepository).save(client);
    }

    @Test
    void shouldThrowException_whenClientNotFoundOnGeneratePassword() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> clientService.generatePassword(CLIENT_ID));
    }

    // createClient()

    @Test
    void shouldCreateClientSuccessfully() {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setEmail(NEW_EMAIL);
        Client client = new Client();
        String rawPassword = "generatedPassword";

        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(employeeRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(passwordGenerator.generatePassword(10)).thenReturn(rawPassword);
        when(clientMapper.createToEntity(dto)).thenReturn(client);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded");

        String result = clientService.createClient(dto);

        assertEquals(rawPassword, result);
        verify(clientRepository).save(client);
        assertEquals("encoded", client.getPassword());
    }

    @Test
    void shouldThrowException_whenCreatingClientAndEmailExistsInClientRepository() {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setEmail(NEW_EMAIL);

        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clientService.createClient(dto));

        verify(employeeRepository, never()).existsByEmail(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreatingClientAndEmailExistsInEmployeeRepository() {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setEmail(NEW_EMAIL);

        when(clientRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(employeeRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clientService.createClient(dto));

        verify(clientRepository, never()).save(any());
    }
}

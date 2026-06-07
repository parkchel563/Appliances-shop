package com.project.appliances.service.impl;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.exception.ClientNotFoundException;
import com.project.appliances.mapper.ClientMapper;
import com.project.appliances.model.Client;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.specification.ClientSpecification;
import com.project.appliances.service.interfaces.ClientService;
import com.project.appliances.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public ClientUpdateProfileDto getProfileForEdit(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException(email));

        return clientMapper.toUpdateProfileDto(client);
    }

    @Override
    @Transactional
    public boolean updateProfile(String currentUserEmail, ClientUpdateProfileDto clientUpdateProfileDto) {
        Client client = clientRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ClientNotFoundException(currentUserEmail));

        boolean emailChanged = !client.getEmail().equals(clientUpdateProfileDto.getEmail());

        if (!client.getEmail().equals(clientUpdateProfileDto.getEmail()) && clientRepository.existsByEmail(clientUpdateProfileDto.getEmail())) {
            log.warn("BUSINESS EVENT | Email already in use | currentEmail={} newEmail={}",
                    currentUserEmail, clientUpdateProfileDto.getEmail());

            throw new IllegalArgumentException("validation.email.exist");
        }
        clientMapper.updateEntity(clientUpdateProfileDto, client);
        clientRepository.save(client);

        log.info("BUSINESS EVENT | Client profile updated | email={} emailChanged={}",
                currentUserEmail, emailChanged);

        return emailChanged;
    }

    @Override
    @Transactional
    public void updateClientProfile(Long id, ClientUpdateProfileDto clientUpdateProfileDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (!client.getEmail().equals(clientUpdateProfileDto.getEmail()) && clientRepository.existsByEmail(clientUpdateProfileDto.getEmail())) {
            log.warn("BUSINESS EVENT | Email already in use (admin update) | id={} email={}",
                    id, clientUpdateProfileDto.getEmail());

            throw new IllegalArgumentException("validation.email.exist");
        }

        clientMapper.updateEntity(clientUpdateProfileDto, client);
        clientRepository.save(client);

        log.info("BUSINESS EVENT | Client updated by admin | id={} email={}", id, clientUpdateProfileDto.getEmail());
    }

    @Override
    @Transactional
    public ClientUpdateProfileDto getClientProfile(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        return clientMapper.toUpdateProfileDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> findAll(ClientSearchCriteria criteria, Pageable pageable) {
        return clientRepository.findAll(ClientSpecification.createSpecification(criteria), pageable)
                .map(clientMapper::toDto);
    }

    @Override
    public void deleteClientProfile(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

//        boolean hasOrders = ordersRepository.existsByClientIdAndStatusIn(id, List.of(OrderStatus.NEW, OrderStatus.CANCELED, OrderStatus.SUBMITTED, OrderStatus.PROCESSING, OrderStatus.READY, OrderStatus.COMPLETED));
//
//        if (hasOrders) {
//            log.warn("BUSINESS EVENT | Cannot delete client with orders | id={}", id);
//            throw new IllegalStateException("client.delete.active.orders");
//        }

        clientRepository.delete(client);
        log.info("BUSINESS EVENT | Client deleted | id={} email={}", id, client.getEmail());
    }

    @Override
    @Transactional
    public String generatePassword(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        String rawPassword = passwordGenerator.generatePassword(10);
        client.setPassword(passwordEncoder.encode(rawPassword));
        clientRepository.save(client);

        log.info("BUSINESS EVENT | Password generated for client | id={}", id);

        return rawPassword;
    }

    @Override
    @Transactional
    public String createClient(ClientCreateDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail()) || employeeRepository.existsByEmail(dto.getEmail())) {
            log.warn("SECURITY EVENT | Registration attempt with already used email '{}'", dto.getEmail());
            throw new IllegalStateException("Email already in use");
        }

        String rawPassword = passwordGenerator.generatePassword(10);

        Client client = clientMapper.createToEntity(dto);
        client.setPassword(passwordEncoder.encode(rawPassword));

        clientRepository.save(client);

        log.info("BUSINESS EVENT | Client '{}' registered successfully", dto.getEmail());

        return rawPassword;
    }
}

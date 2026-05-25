package com.project.appliances.service.impl;

import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.mapper.ClientMapper;
import com.project.appliances.model.Client;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientMapper clientMapper;

    @Override
    public void registerClient(ClientRegistrationDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail()) || employeeRepository.existsByEmail(dto.getEmail())) {
            log.warn("SECURITY EVENT | Registration attempt with already used email '{}'", dto.getEmail());
            throw new IllegalStateException("Email already in use");
        }

        Client client = clientMapper.toEntity(dto);
        client.setPassword(passwordEncoder.encode(dto.getPassword()));

        clientRepository.save(client);

        log.info("BUSINESS EVENT | Client '{}' registered successfully", dto.getEmail());
    }
}

package com.project.appliances.service.interfaces;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    ClientUpdateProfileDto getProfileForEdit(String email);

    boolean updateProfile(String currentUserEmail, ClientUpdateProfileDto clientUpdateProfileDto);

    void updateClientProfile(Long id, ClientUpdateProfileDto clientUpdateProfileDto);

    ClientUpdateProfileDto getClientProfile(Long id);

    List<ClientDto> getAll();

    Page<ClientDto> findAll(ClientSearchCriteria criteria, Pageable pageable);

    void deleteClientProfile(Long id);

    String generatePassword(Long id);

    String createClient(ClientCreateDto dto);
}

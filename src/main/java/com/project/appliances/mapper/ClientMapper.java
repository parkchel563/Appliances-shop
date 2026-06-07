package com.project.appliances.mapper;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.model.Client;

public interface ClientMapper {
    ClientUpdateProfileDto toUpdateProfileDto(Client client);

    Client updateEntity(ClientUpdateProfileDto clientUpdateProfileDto, Client client);

    Client toEntity(ClientRegistrationDto clientRegistrationDto);

    ClientDto toDto(Client client);

    Client createToEntity(ClientCreateDto clientCreateDto);
}

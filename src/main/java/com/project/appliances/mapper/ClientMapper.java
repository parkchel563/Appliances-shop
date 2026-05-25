package com.project.appliances.mapper;

import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.model.Client;

public interface ClientMapper {
    Client toEntity(ClientRegistrationDto clientRegistrationDto);

    ClientDto toDto(Client client);
}

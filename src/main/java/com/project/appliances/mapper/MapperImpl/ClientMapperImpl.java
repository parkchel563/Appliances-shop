package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.mapper.ClientMapper;
import com.project.appliances.model.Client;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMapperImpl implements ClientMapper {

    private final ModelMapper modelMapper;

    @Override
    public Client toEntity(ClientRegistrationDto clientRegistrationDto) {
        return modelMapper.map(clientRegistrationDto, Client.class);
    }

    @Override
    public ClientDto toDto(Client client) {
        return modelMapper.map(client, ClientDto.class);
    }
}

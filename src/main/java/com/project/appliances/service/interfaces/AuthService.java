package com.project.appliances.service.interfaces;

import com.project.appliances.dto.client.ClientRegistrationDto;

public interface AuthService {
    void registerClient(ClientRegistrationDto dto);
}

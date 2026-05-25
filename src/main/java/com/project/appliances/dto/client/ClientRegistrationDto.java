package com.project.appliances.dto.client;

import com.project.appliances.dto.user.UserRegistrationDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRegistrationDto extends UserRegistrationDto{
    @NotBlank(message = "{validation.card.notBlank}")
    private String card;
}
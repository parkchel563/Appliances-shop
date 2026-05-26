package com.project.appliances.dto.client;

import com.project.appliances.dto.user.UserRegistrationDto;
import com.project.appliances.validation.PasswordConfirmation;
import com.project.appliances.validation.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatches
public class ClientRegistrationDto extends UserRegistrationDto implements PasswordConfirmation {
    @NotBlank(message = "{validation.card.notBlank}")
    private String card;
}
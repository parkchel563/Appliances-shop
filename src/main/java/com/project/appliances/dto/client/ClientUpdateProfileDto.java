package com.project.appliances.dto.client;

import com.project.appliances.dto.user.UserUpdateDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientUpdateProfileDto extends UserUpdateDto {
    @NotBlank(message = "{validation.card.notBlank}")
    private String card;
}


package com.project.appliances.dto.client;

import com.project.appliances.dto.user.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ClientDto extends UserDto {
    private String card;
}

package com.project.appliances.dto.user;

import lombok.Data;

@Data
public abstract class UserDto {
    private Long id;
    private String name;
    private String email;
}
package com.project.appliances.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class UserUpdateDto {
    @NotBlank(message = "{validation.name.notBlank}")
    @Size(min = 4, max = 100, message = "{validation.name.size}")
    private String name;

    @NotBlank(message = "{validation.email.notBlank}")
    @Email(message = "{validation.email.invalid}")
    private String email;
}

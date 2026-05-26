package com.project.appliances.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class UserRegistrationDto {
    @NotBlank(message = "{validation.name.notBlank}")
    @Size(min = 3, max = 30, message = "{validation.name.size}")
    private String name;

    @NotBlank(message = "{validation.email.notBlank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 6, max = 100, message = "{validation.password.size}")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
            message = "{validation.password.strong}"
    )
    private String password;

    @NotBlank(message = "{validation.confirmPassword.notBlank}")
    private String confirmPassword;
}

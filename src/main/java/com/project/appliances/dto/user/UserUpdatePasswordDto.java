package com.project.appliances.dto.user;

import com.project.appliances.validation.PasswordConfirmation;
import com.project.appliances.validation.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatches
public class UserUpdatePasswordDto implements PasswordConfirmation {
    @NotBlank(message = "{validation.required.password}")
    private String currentPassword;

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

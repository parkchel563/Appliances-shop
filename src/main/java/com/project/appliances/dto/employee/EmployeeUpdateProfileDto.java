package com.project.appliances.dto.employee;

import com.project.appliances.dto.user.UserUpdateDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeUpdateProfileDto extends UserUpdateDto {
    @NotBlank(message = "{validation.department.notBlank}")
    @Size(min = 4, max = 30, message = "{validation.department.size}")
    String department;
}


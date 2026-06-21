package com.project.appliances.dto.employee;

import com.project.appliances.dto.user.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeDto extends UserDto {
    private String department;

    private boolean canBeDeleted;
}

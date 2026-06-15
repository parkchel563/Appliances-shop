package com.project.appliances.mapper;

import com.project.appliances.dto.employee.EmployeeCreateDto;
import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.model.Employee;

public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);

    EmployeeUpdateProfileDto toUpdateProfileDto(Employee employee);

    Employee toUpdateEntity(EmployeeUpdateProfileDto employeeUpdateProfileDto, Employee employee);

    Employee createToEntity(EmployeeCreateDto employeeCreateDto);
}

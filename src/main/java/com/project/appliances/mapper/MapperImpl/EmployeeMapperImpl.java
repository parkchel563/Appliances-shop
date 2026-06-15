package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.employee.EmployeeCreateDto;
import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.mapper.EmployeeMapper;
import com.project.appliances.model.Employee;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMapperImpl implements EmployeeMapper {

    private final ModelMapper modelMapper;

    @Override
    public EmployeeDto toDto(Employee employee) {
        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    public EmployeeUpdateProfileDto toUpdateProfileDto(Employee employee) {
        return modelMapper.map(employee, EmployeeUpdateProfileDto.class);
    }

    @Override
    public Employee toUpdateEntity(EmployeeUpdateProfileDto employeeUpdateProfileDto, Employee employee) {
        modelMapper.map(employeeUpdateProfileDto, employee);
        return employee;
    }

    @Override
    public Employee createToEntity(EmployeeCreateDto employeeCreateDto) {
        return modelMapper.map(employeeCreateDto, Employee.class);
    }
}

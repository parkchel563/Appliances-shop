package com.project.appliances.service.interfaces;

import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    List<EmployeeDto> findAll();

    Page<EmployeeDto> findAll(EmployeeSearchCriteria employeeSearchCriteria, Pageable pageable);

    EmployeeUpdateProfileDto getEmployeeProfile(Long id);

    void updateEmployeeProfile(Long id, EmployeeUpdateProfileDto dto);

    String generatePassword(Long id);
}

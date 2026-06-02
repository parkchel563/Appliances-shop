package com.project.appliances.exception;

import com.project.appliances.constants.MessageKeys;

public class EmployeeNotFoundException extends ResourceNotFoundException {
    public EmployeeNotFoundException(Long id) {
        super(MessageKeys.EMPLOYEE_NOT_FOUND, id);
    }
}

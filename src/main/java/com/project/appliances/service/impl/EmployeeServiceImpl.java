package com.project.appliances.service.impl;

import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.exception.EmployeeNotFoundException;
import com.project.appliances.mapper.EmployeeMapper;
import com.project.appliances.model.Employee;
import com.project.appliances.model.OrderStatus;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.repository.specification.EmployeeSpecification;
import com.project.appliances.service.interfaces.EmployeeService;
import com.project.appliances.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final OrdersRepository ordersRepository;


    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> findAll(EmployeeSearchCriteria criteria, Pageable pageable) {
        return employeeRepository.findAll(EmployeeSpecification.createSpecification(criteria), pageable)
                .map(employeeMapper::toDto);
    }

    @Override
    @Transactional
    public EmployeeUpdateProfileDto getEmployeeProfile(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return employeeMapper.toUpdateProfileDto(employee);
    }

    @Override
    @Transactional
    public void updateEmployeeProfile(Long id, EmployeeUpdateProfileDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        String newEmail = dto.getEmail();

        if (!employee.getEmail().equals(newEmail)) {
            if (employeeRepository.existsByEmail(newEmail)
                    || clientRepository.existsByEmail(newEmail)) {

                log.warn("BUSINESS EVENT | Email already in use | id={} email={}", id, newEmail);
                throw new IllegalStateException("Email already exists");
            }
        }

        employeeMapper.toUpdateEntity(dto, employee);
        employeeRepository.save(employee);

        log.info("BUSINESS EVENT | Employee profile updated | id={} email={}", id, newEmail);
    }

    @Override
    @Transactional
    public String generatePassword(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        String rawPassword = passwordGenerator.generatePassword(10);

        employee.setPassword(passwordEncoder.encode(rawPassword));

        employeeRepository.save(employee);

        log.info("BUSINESS EVENT | Password generated for employee | id={}", id);

        return rawPassword;
    }

    @Override
    @Transactional
    public boolean deleteEmployeeProfile(Long id, String currentUserEmail) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        boolean hasOrders = ordersRepository.existsByEmployeeIdAndStatusIn(id, List.of(OrderStatus.PROCESSING, OrderStatus.READY, OrderStatus.COMPLETED, OrderStatus.CANCELED));

        if (hasOrders) {
            log.warn("BUSINESS EVENT | Cannot delete employee with orders | id={}", id);
            throw new IllegalStateException("Employee has orders");
        }

        boolean deleteSelf = employee.getEmail().equals(currentUserEmail);

        employeeRepository.delete(employee);
        log.info("BUSINESS EVENT | Client deleted | id={} email={}", id, employee.getEmail());
        return deleteSelf;
    }
}

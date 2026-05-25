package com.project.appliances.repository;

import com.project.appliances.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);


}

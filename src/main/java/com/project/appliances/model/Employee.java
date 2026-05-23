package com.project.appliances.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee")
public class Employee extends  User{

    @Column(name = "department")
    private String department;

    public Employee(Long id, String name, String email, String password, Integer failedAttempts, Boolean accountNonLocked, LocalDateTime lockTime, String department) {
        super(id, name, email, password, failedAttempts, accountNonLocked, lockTime);
        this.department = department;
    }
}

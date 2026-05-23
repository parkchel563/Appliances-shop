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
@Table(name = "client")
public class Client extends User{
    @Column(name = "card")
    private String card;

    public Client(Long id, String name, String email, String password, Integer failedAttempts, Boolean accountNonLocked, LocalDateTime lockTime, String card) {
        super(id, name, email, password, failedAttempts, accountNonLocked, lockTime);
        this.card = card;
    }

}

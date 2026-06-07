package com.project.appliances.repository;

import com.project.appliances.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Client> findAll();
}

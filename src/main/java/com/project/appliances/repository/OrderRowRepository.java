package com.project.appliances.repository;

import com.project.appliances.model.OrderRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRowRepository extends JpaRepository<OrderRow, Long> {
    boolean existsByApplianceId(Long id);
}

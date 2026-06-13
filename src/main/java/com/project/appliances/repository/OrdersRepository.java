package com.project.appliances.repository;

import com.project.appliances.model.OrderStatus;
import com.project.appliances.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {
    Optional<Orders> findByClientEmailAndStatus(String email, OrderStatus status);

    List<Orders> findAllByStatusNot(OrderStatus status, Sort sort);

    Optional<Orders> findById(Long id);

    Page<Orders> findByClientEmailOrderByIdDesc(String email, Pageable pageable);

    boolean existsByClientIdAndStatusIn(Long clientId, Collection<OrderStatus> status);

    boolean existsByEmployeeIdAndStatusIn(Long employeeId, Collection<OrderStatus> status);
}

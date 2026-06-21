package com.project.appliances.service.interfaces;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.dto.orders.OrdersSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrdersService {
    List<OrdersAdminDto> findAllOrders();

    Page<OrdersAdminDto> findAllOrders(OrdersSearchCriteria criteria, Pageable pageable);

    OrderDetailsDto findOrderDetailsById(Long orderId);

    void takeOrderInWork(Long orderId, String employeeEmail);

    void markAsReady(Long orderId, String employeeEmail);

    void markAsCompleted(Long orderId, String employeeEmail);

    void markAsCancelled(Long orderId, String employeeEmail);

    Page<OrdersAdminDto> findAllOrdersByClientEmail(String clientEmail, Pageable pageable);

    OrderDetailsDto findOrderDetails(Long orderId, String clientEmail);

    void cancelOrder(Long orderId, String clientEmail);


}
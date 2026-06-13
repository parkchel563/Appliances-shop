package com.project.appliances.service.interfaces;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdersService {

    Page<OrdersAdminDto> findAllOrdersByClientEmail(String clientEmail, Pageable pageable);

    OrderDetailsDto findOrderDetails(Long orderId, String clientEmail);

    void cancelOrder(Long orderId, String clientEmail);


}
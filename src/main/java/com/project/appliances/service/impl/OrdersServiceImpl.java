package com.project.appliances.service.impl;


import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.exception.OrderNotFoundException;
import com.project.appliances.mapper.OrdersAdminMapper;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.OrderStatus;
import com.project.appliances.model.Orders;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.interfaces.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrdersMapper ordersMapper;
    private final OrdersAdminMapper ordersAdminMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrdersAdminDto> findAllOrdersByClientEmail(String clientEmail, Pageable pageable) {
        return ordersRepository.findByClientEmailOrderByIdDesc(clientEmail, pageable)
                .map(ordersAdminMapper::toAdminDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsDto findOrderDetails(Long orderId, String clientEmail) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getClient() == null || !order.getClient().getEmail().equals(clientEmail)) {
            log.warn("SECURITY EVENT | Unauthorized access to order | orderId={} client={}", orderId, clientEmail);
            throw new AccessDeniedException("You do not have access to this order");
        }

        return ordersMapper.DetailsToDto(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String clientEmail) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getClient() == null || !order.getClient().getEmail().equals(clientEmail)) {
            log.warn("SECURITY EVENT | Unauthorized cancel attempt | orderId={} client={}", orderId, clientEmail);
            throw new AccessDeniedException("You do not have access to this order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.READY) {
            log.warn("BUSINESS EVENT | Invalid cancel attempt | orderId={} status={}", orderId, order.getStatus());
            throw new IllegalArgumentException("Completed order cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELED);
        log.info("BUSINESS EVENT | Order cancelled by client | orderId={} client={}", orderId, clientEmail);
    }

    private void checkAssignedEmployee(Orders order, String email) {
        if (order.getEmployee() == null || !order.getEmployee().getEmail().equals(email)) {
            log.warn("SECURITY EVENT | Employee not assigned | orderId={} employee={}", order.getId(), email);
            throw new AccessDeniedException("You are not assigned to this order");
        }
    }
}

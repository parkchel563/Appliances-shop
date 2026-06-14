package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.exception.OrderNotFoundException;
import com.project.appliances.mapper.OrdersAdminMapper;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.OrderStatus;
import com.project.appliances.model.Orders;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.impl.OrdersServiceImpl;
import com.project.appliances.util.TestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplTest {
    private static final Long ORDER_ID = 100L;
    private static final String EMPLOYEE_EMAIL = "worker@test.com";
    private static final String CLIENT_EMAIL = "client@test.com";
    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrdersAdminMapper ordersAdminMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private OrdersServiceImpl ordersService;

    @Test
    void shouldFindOrderDetailsForClientSuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(TestDataFactory.createClient(1L, CLIENT_EMAIL));
        OrderDetailsDto dto = new OrderDetailsDto();

        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(ordersMapper.DetailsToDto(order)).thenReturn(dto);

        OrderDetailsDto result = ordersService.findOrderDetails(ORDER_ID, CLIENT_EMAIL);
        assertEquals(dto, result);
    }

    @Test
    void shouldThrowException_whenFindDetailsAndClientIsNull() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(null);
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.findOrderDetails(ORDER_ID, CLIENT_EMAIL));
    }

    @Test
    void shouldThrowException_whenFindDetailsAndClientEmailMismatch() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(TestDataFactory.createClient(1L, "wrong@client.com"));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.findOrderDetails(ORDER_ID, CLIENT_EMAIL));
    }

    // cancelOrder()
    @Test
    void shouldCancelOrderByClientSuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(TestDataFactory.createClient(1L, CLIENT_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void shouldThrowException_whenClientCancelsCompletedOrder() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.COMPLETED);
        order.setClient(TestDataFactory.createClient(1L, CLIENT_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL));
    }

    @Test
    void shouldThrowException_whenClientCancelsReadyOrder() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.READY);
        order.setClient(TestDataFactory.createClient(1L, CLIENT_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL));
    }

    @Test
    void shouldThrowException_whenCancelAndClientIsNull() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(null);
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL));
    }

    @Test
    void shouldThrowException_whenOrderNotFoundOnCancel() {
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL));
    }

    @Test
    void shouldThrowException_whenCancelAndClientEmailMismatch() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setClient(TestDataFactory.createClient(1L, "wrong-hacker@email.com"));

        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.cancelOrder(ORDER_ID, CLIENT_EMAIL));
    }

    // findAllOrdersByClientEmail()
    @Test
    void shouldFindAllOrdersByClientEmail_whenOrdersExist() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        OrdersAdminDto dto = new OrdersAdminDto();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Orders> ordersPage = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByClientEmailOrderByIdDesc(CLIENT_EMAIL, pageable))
                .thenReturn(ordersPage);
        when(ordersAdminMapper.toAdminDto(order)).thenReturn(dto);

        Page<OrdersAdminDto> result = ordersService.findAllOrdersByClientEmail(CLIENT_EMAIL, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(dto, result.getContent().getFirst());

        verify(ordersRepository).findByClientEmailOrderByIdDesc(CLIENT_EMAIL, pageable);
        verify(ordersAdminMapper).toAdminDto(order);
    }

    @Test
    void shouldReturnEmptyPage_whenNoOrdersFoundByClientEmail() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Orders> emptyPage = Page.empty(pageable);

        when(ordersRepository.findByClientEmailOrderByIdDesc(CLIENT_EMAIL, pageable))
                .thenReturn(emptyPage);

        Page<OrdersAdminDto> result = ordersService.findAllOrdersByClientEmail(CLIENT_EMAIL, pageable);

        assertTrue(result.isEmpty());

        verify(ordersRepository).findByClientEmailOrderByIdDesc(CLIENT_EMAIL, pageable);
        verify(ordersAdminMapper, never()).toAdminDto(any());
    }
}


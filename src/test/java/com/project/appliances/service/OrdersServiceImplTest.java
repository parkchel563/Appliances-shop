package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.dto.orders.OrdersSearchCriteria;
import com.project.appliances.exception.OrderNotFoundException;
import com.project.appliances.mapper.OrdersAdminMapper;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.Employee;
import com.project.appliances.model.OrderStatus;
import com.project.appliances.model.Orders;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.impl.OrdersServiceImpl;
import com.project.appliances.util.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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

    // findAllOrders()
    @Test
    void shouldFindAllOrders() {
        Orders orders = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        OrdersAdminDto dto = new OrdersAdminDto();

        when(ordersRepository.findAllByStatusNot(eq(OrderStatus.NEW), any(Sort.class)))
                .thenReturn(List.of(orders));
        when(ordersAdminMapper.toAdminDto(orders)).thenReturn(dto);

        List<OrdersAdminDto> result = ordersService.findAllOrders();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void shouldFindAllOrdersWithPagination() {
        OrdersSearchCriteria criteria = new OrdersSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        OrdersAdminDto dto = new OrdersAdminDto();
        Page<Orders> page = new PageImpl<>(List.of(order));

        when(ordersRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(ordersAdminMapper.toAdminDto(order)).thenReturn(dto);

        Page<OrdersAdminDto> result = ordersService.findAllOrders(criteria, pageable);

        assertEquals(1, result.getTotalElements());
    }

    // findOrderDetailsById()
    @Test
    void shouldFindOrderDetailsById() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        OrderDetailsDto dto = new OrderDetailsDto();

        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(ordersMapper.DetailsToDto(order)).thenReturn(dto);

        OrderDetailsDto result = ordersService.findOrderDetailsById(ORDER_ID);
        assertEquals(dto, result);
    }

    @Test
    void shouldThrowException_whenOrderNotFoundById() {
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> ordersService.findOrderDetailsById(ORDER_ID));
    }

    // takeOrderInWork()
    @Test
    void shouldTakeOrderInWorkSuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        Employee employee = TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL);

        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(employee));

        ordersService.takeOrderInWork(ORDER_ID, EMPLOYEE_EMAIL);

        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(employee, order.getEmployee());
    }

    @Test
    void shouldThrowException_whenTakingOrderWithInvalidStatus() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.NEW);
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.takeOrderInWork(ORDER_ID, EMPLOYEE_EMAIL));
    }

    @Test
    void shouldThrowException_whenTakingOrderAndEmployeeNotFound() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ordersService.takeOrderInWork(ORDER_ID, EMPLOYEE_EMAIL));
    }

    // Employee Status Updates (Ready, Completed, Cancelled)
    @Test
    void shouldMarkAsReadySuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.PROCESSING);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        ordersService.markAsReady(ORDER_ID, EMPLOYEE_EMAIL);
        assertEquals(OrderStatus.READY, order.getStatus());
    }

    @Test
    void shouldThrowException_whenMarkingReadyWithInvalidStatus() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.SUBMITTED);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.markAsReady(ORDER_ID, EMPLOYEE_EMAIL));
    }

    @Test
    void shouldMarkAsCompletedSuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.READY);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        ordersService.markAsCompleted(ORDER_ID, EMPLOYEE_EMAIL);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldThrowException_whenMarkingCompletedWithInvalidStatus() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.PROCESSING);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.markAsCompleted(ORDER_ID, EMPLOYEE_EMAIL));
    }

    @Test
    void shouldMarkAsCancelledByEmployeeSuccessfully() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.PROCESSING);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        ordersService.markAsCancelled(ORDER_ID, EMPLOYEE_EMAIL);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void shouldThrowException_whenEmployeeCancelsCompletedOrder() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.COMPLETED);
        order.setEmployee(TestDataFactory.createEmployee(1L, EMPLOYEE_EMAIL));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> ordersService.markAsCancelled(ORDER_ID, EMPLOYEE_EMAIL));
    }

    // checkAssignedEmployee()
    @Test
    void shouldThrowException_whenEmployeeIsNull() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.PROCESSING);
        order.setEmployee(null);
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.markAsReady(ORDER_ID, EMPLOYEE_EMAIL));
    }

    @Test
    void shouldThrowException_whenEmployeeEmailDoesNotMatch() {
        Orders order = TestDataFactory.createOrder(ORDER_ID, OrderStatus.PROCESSING);
        order.setEmployee(TestDataFactory.createEmployee(1L, "wrong@test.com"));
        when(ordersRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> ordersService.markAsReady(ORDER_ID, EMPLOYEE_EMAIL));
    }

    // findOrderDetails()
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
        assertEquals(dto, result.getContent().get(0));

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


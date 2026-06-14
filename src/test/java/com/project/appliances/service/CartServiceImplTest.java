package com.project.appliances.service;


import static com.project.appliances.util.TestDataFactory.createTestCart;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.project.appliances.dto.orders.OrdersDto;
import com.project.appliances.exception.ApplianceNotFoundException;
import com.project.appliances.exception.ClientNotFoundException;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.*;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.OrderRowRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.impl.CartServiceImpl;
import com.project.appliances.util.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final String EMAIL = "test@user.com";
    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private OrderRowRepository orderRowRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ApplianceRepository applianceRepository;
    @Mock
    private OrdersMapper ordersMapper;
    @InjectMocks
    private CartServiceImpl cartService;

    // getCart()
    @Test
    void shouldReturnExistingCart() {
        Orders cart = new Orders();
        OrdersDto dto = new OrdersDto();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW))
                .thenReturn(Optional.of(cart));
        when(ordersMapper.toDto(cart)).thenReturn(dto);

        OrdersDto result = cartService.getCart(EMAIL);

        assertEquals(dto, result);
        verify(ordersMapper).toDto(cart);
    }

    @Test
    void shouldReturnEmptyCart() {
        OrdersDto dto = new OrdersDto();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW))
                .thenReturn(Optional.empty());

        when(ordersMapper.toDto(any(Orders.class))).thenReturn(dto);

        OrdersDto result = cartService.getCart(EMAIL);

        assertEquals(dto, result);
        verify(ordersMapper).toDto(any(Orders.class));
    }

    // addToCard()
    @Test
    void shouldAddNewItemToCart() {
        Orders cart = createTestCart();
        Appliance appliance = TestDataFactory.createAppliance();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW))
                .thenReturn(Optional.of(cart));
        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));

        cartService.addToCart(EMAIL, 1L);

        assertEquals(1, cart.getOrderRows().size());
        assertEquals(BigDecimal.valueOf(100), cart.getTotal());

        verify(orderRowRepository).save(any(OrderRow.class));
        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldIncreaseQuantity_whenItemAlreadyInCart() {
        Orders cart = createTestCart();
        Appliance appliance = TestDataFactory.createAppliance();
        appliance.setId(1L);
        OrderRow row = new OrderRow();
        row.setAppliance(appliance);
        row.setQuantity(1L);
        row.setTotal(BigDecimal.valueOf(100));
        cart.getOrderRows().add(row);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW))
                .thenReturn(Optional.of(cart));
        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));

        cartService.addToCart(EMAIL, 1L);

        assertEquals(2L, row.getQuantity());
        assertEquals(BigDecimal.valueOf(200), row.getTotal());
        assertEquals(BigDecimal.valueOf(200), cart.getTotal());

        verify(orderRowRepository).save(row);
        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldCreateNewCart_whenAddingItemAndCartNotFound() {
        Client client = new Client();
        Appliance appliance = TestDataFactory.createAppliance();
        appliance.setPrice(BigDecimal.valueOf(100));
        Orders newCart = createTestCart();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(ordersRepository.save(any(Orders.class))).thenReturn(newCart);
        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));

        cartService.addToCart(EMAIL, 1L);

        verify(clientRepository).findByEmail(EMAIL);
        verify(ordersRepository).save(newCart);
    }

    @Test
    void shouldThrowException_whenApplianceNotFoundOnAdd() {
        Orders cart = createTestCart();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));
        when(applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class, () -> cartService.addToCart(EMAIL, 1L));
        verify(orderRowRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenClientNotFoundCreatingCart() {
        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> cartService.addToCart(EMAIL, 1L));
    }

    // removeFromCart()
    @Test
    void shouldRemoveItemAndRecalculatePrice() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        OrderRow row2 = TestDataFactory.createOrderRow(2L, BigDecimal.valueOf(200));
        cart.getOrderRows().add(row1);
        cart.getOrderRows().add(row2);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        cartService.removeFromCart(EMAIL, 1L);

        assertEquals(1, cart.getOrderRows().size());
        assertEquals(BigDecimal.valueOf(200), cart.getTotal());

        verify(orderRowRepository).delete(row1);
        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldRemoveItemAndDeleteCart_whenCartBecomesEmpty() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        cart.getOrderRows().add(row1);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        cartService.removeFromCart(EMAIL, 1L);

        assertTrue(cart.getOrderRows().isEmpty());

        verify(orderRowRepository).delete(row1);
        verify(ordersRepository).delete(cart);
    }

    @Test
    void shouldThrowException_whenOrderRowNotFoundOnRemove() {
        Orders cart = createTestCart();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        assertThrows(EntityNotFoundException.class, () -> cartService.removeFromCart(EMAIL, 99L));
    }

    // updateQuantity
    @Test
    void shouldUpdateQuantityAndRecalculatePrice() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        cart.getOrderRows().add(row1);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        cartService.updateQuantity(EMAIL, 1L, 3L);

        assertEquals(3L, row1.getQuantity());
        assertEquals(BigDecimal.valueOf(300), cart.getTotal());
        assertEquals(BigDecimal.valueOf(300), row1.getTotal());

        verify(orderRowRepository).save(row1);
        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldRemoveItem_whenQuantitySetToZero_andCartHasOtherItems() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        OrderRow row2 = TestDataFactory.createOrderRow(2L, BigDecimal.valueOf(200));
        cart.getOrderRows().add(row1);
        cart.getOrderRows().add(row2);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        cartService.updateQuantity(EMAIL, 1L, 0L);

        assertEquals(1, cart.getOrderRows().size());
        assertEquals(BigDecimal.valueOf(200), cart.getTotal());

        verify(orderRowRepository).delete(row1);
        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldRemoveItemAndDeleteCart_whenQuantitySetToZero_andCartBecomesEmpty() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        cart.getOrderRows().add(row1);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW))
                .thenReturn(Optional.of(cart));

        cartService.updateQuantity(EMAIL, 1L, 0L);

        assertTrue(cart.getOrderRows().isEmpty());

        verify(orderRowRepository).delete(row1);
        verify(ordersRepository).delete(cart);
    }

    @Test
    void shouldThrowException_whenOrderRowNotFoundOnUpdateQuantity() {
        Orders cart = createTestCart();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        assertThrows(EntityNotFoundException.class, () -> cartService.updateQuantity(EMAIL, 99L, 5L));
    }

    // submitOrder()
    @Test
    void shouldSubmitOrderSuccessfully() {
        Orders cart = createTestCart();
        OrderRow row1 = TestDataFactory.createOrderRow(1L, BigDecimal.valueOf(100));
        cart.getOrderRows().add(row1);

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        cartService.submitOrder(EMAIL);

        assertEquals(OrderStatus.SUBMITTED, cart.getStatus());

        verify(ordersRepository).save(cart);
    }

    @Test
    void shouldThrowException_whenSubmittingEmptyCart() {
        Orders cart = createTestCart();

        when(ordersRepository.findByClientEmailAndStatus(EMAIL, OrderStatus.NEW)).thenReturn(Optional.of(cart));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> cartService.submitOrder(EMAIL));

        assertEquals("Cart is empty", ex.getMessage());

        verify(ordersRepository, never()).save(any());
    }
}

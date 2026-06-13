package com.project.appliances.service.impl;

import com.project.appliances.dto.orders.OrdersDto;
import com.project.appliances.exception.ApplianceNotFoundException;
import com.project.appliances.exception.ClientNotFoundException;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.*;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.OrderRowRepository;
import com.project.appliances.repository.OrdersRepository;
import com.project.appliances.service.interfaces.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrdersRepository ordersRepository;
    private final OrderRowRepository orderRowRepository;
    private final ClientRepository clientRepository;
    private final ApplianceRepository applianceRepository;
    private final OrdersMapper ordersMapper;

    @Override
    @Transactional(readOnly = true)
    public OrdersDto getCart(String email) {
        return ordersRepository.findByClientEmailAndStatus(email, OrderStatus.NEW)
                .map(ordersMapper::toDto)
                .orElseGet(() -> {
                    Orders emptyCart = new Orders();
                    emptyCart.setStatus(OrderStatus.NEW);
                    emptyCart.setTotal(BigDecimal.ZERO);
                    emptyCart.setOrderRows(new HashSet<>());
                    return ordersMapper.toDto(emptyCart);
                });
    }

    @Override
    @Transactional
    public void addToCart(String email, Long applianceId) {
        Orders cart = getOrCreateCart(email);

        Appliance appliance = applianceRepository.findById(applianceId)
                .orElseThrow(() -> new ApplianceNotFoundException(applianceId));

        OrderRow existingRow = cart.getOrderRows().stream()
                .filter(row -> row.getAppliance().getId().equals(applianceId))
                .findFirst().orElse(null);

        if (existingRow == null) {
            OrderRow orderRow = new OrderRow();
            orderRow.setAppliance(appliance);
            orderRow.setQuantity(1L);
            orderRow.setTotal(appliance.getPrice());

            orderRowRepository.save(orderRow);
            cart.getOrderRows().add(orderRow);
            log.info("BUSINESS EVENT | Item added to cart | email={} applianceId={}", email, applianceId);
        } else {
            long newQuantity = existingRow.getQuantity() + 1;
            existingRow.setQuantity(newQuantity);
            existingRow.setTotal(appliance.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            orderRowRepository.save(existingRow);
            log.info("BUSINESS EVENT | Item quantity increased | email={} applianceId={} quantity={}",
                    email, applianceId, newQuantity);
        }
        recalculateTotal(cart);
        ordersRepository.save(cart);
    }

    @Override
    public void removeFromCart(String email, Long orderRowId) {
        Orders cart = getOrCreateCart(email);

        OrderRow orderRow = cart.getOrderRows().stream()
                .filter(row -> row.getId().equals(orderRowId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("order row not found"));

        cart.getOrderRows().remove(orderRow);
        orderRowRepository.delete(orderRow);

        if (cart.getOrderRows().isEmpty()) {
            ordersRepository.delete(cart);
            log.info("BUSINESS EVENT | Cart emptied and deleted | email={}", email);
            return;
        }

        recalculateTotal(cart);
        ordersRepository.save(cart);
        log.info("BUSINESS EVENT | Item removed from cart | email={} orderRowId={}", email, orderRowId);
    }

    @Override
    @Transactional
    public void updateQuantity(String email, Long orderRowId, Long quantity) {
        Orders cart = getOrCreateCart(email);

        OrderRow orderRow = cart.getOrderRows().stream()
                .filter(row -> row.getId().equals(orderRowId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("order row not found"));

        if (quantity <= 0) {
            cart.getOrderRows().remove(orderRow);
            orderRowRepository.delete(orderRow);

            if (cart.getOrderRows().isEmpty()) {
                ordersRepository.delete(cart);
                log.info("BUSINESS EVENT | Cart emptied by quantity update | email={}", email);
                return;
            }
            log.info("BUSINESS EVENT | Item removed due to zero quantity | email={} orderRowId={}", email, orderRowId);
        } else {
            orderRow.setQuantity(quantity);
            orderRow.setTotal(
                    orderRow.getAppliance().getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            orderRowRepository.save(orderRow);
            log.info("BUSINESS EVENT | Item quantity updated | email={} orderRowId={} quantity={}",
                    email, orderRowId, quantity);
        }
        recalculateTotal(cart);
        ordersRepository.save(cart);
    }

    @Override
    @Transactional
    public void submitOrder(String email) {
        Orders cart = getOrCreateCart(email);

        if (cart.getOrderRows().isEmpty()) {
            log.warn("BUSINESS EVENT | Attempt to submit empty cart | email={}", email);
            throw new IllegalStateException("Cart is empty");
        }

        cart.setStatus(OrderStatus.SUBMITTED);
        ordersRepository.save(cart);
        log.info("BUSINESS EVENT | Order submitted | email={} orderId={}", email, cart.getId());
    }

    private Orders getOrCreateCart(String email) {
        return ordersRepository.findByClientEmailAndStatus(email, OrderStatus.NEW)
                .orElseGet(() -> {
                    Client client = clientRepository.findByEmail(email)
                            .orElseThrow(() -> new ClientNotFoundException(email));

                    Orders cart = new Orders();
                    cart.setClient(client);
                    cart.setEmployee(null);
                    cart.setStatus(OrderStatus.NEW);
                    cart.setTotal(BigDecimal.ZERO);
                    cart.setOrderRows(new HashSet<>());

                    return ordersRepository.save(cart);
                });
    }

    private void recalculateTotal(Orders cart) {
        BigDecimal total = cart.getOrderRows().stream()
                .map(OrderRow::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotal(total);
    }
}

package com.project.appliances.service.interfaces;

import com.project.appliances.dto.orders.OrdersDto;

public interface CartService {

    OrdersDto getCart(String email);

    void addToCart(String email, Long applianceId);

    void removeFromCart(String email, Long orderRowId);

    void updateQuantity(String email, Long orderRowId, Long quantity);

    void submitOrder(String email);
}

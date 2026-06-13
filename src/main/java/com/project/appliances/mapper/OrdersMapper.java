package com.project.appliances.mapper;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersDto;
import com.project.appliances.model.Orders;

public interface OrdersMapper {

    OrdersDto toDto(Orders orders);

    OrderDetailsDto DetailsToDto(Orders orders);
}

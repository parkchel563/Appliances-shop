package com.project.appliances.mapper;

import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.model.Orders;

public interface OrdersAdminMapper {
    OrdersAdminDto toAdminDto(Orders entity);
}

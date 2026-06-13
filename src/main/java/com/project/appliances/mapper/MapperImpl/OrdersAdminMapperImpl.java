package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.mapper.OrdersAdminMapper;
import com.project.appliances.model.Orders;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdersAdminMapperImpl implements OrdersAdminMapper {

    private final ModelMapper modelMapper;

    @Override
    public OrdersAdminDto toAdminDto(Orders entity) {
        OrdersAdminDto dto = modelMapper.map(entity, OrdersAdminDto.class);

        if (entity.getClient() != null) {
            dto.setClientName(entity.getClient().getName());
            dto.setClientEmail(entity.getClient().getEmail());
        }

        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }

        if (entity.getOrderRows() != null) {
            dto.setQuantity(entity.getOrderRows().size());
        }

        return dto;
    }
}
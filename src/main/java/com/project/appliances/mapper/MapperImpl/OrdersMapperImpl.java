package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.orderRow.OrderRowDetailsDto;
import com.project.appliances.dto.orderRow.OrderRowDto;
import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersDto;
import com.project.appliances.mapper.OrdersMapper;
import com.project.appliances.model.OrderRow;
import com.project.appliances.model.Orders;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrdersMapperImpl implements OrdersMapper {

    private final ModelMapper modelMapper;

    @Override
    public OrdersDto toDto(Orders entity) {
        OrdersDto ordersDto = modelMapper.map(entity, OrdersDto.class);

        if (entity.getOrderRows() != null) {
            List<OrderRowDto> rows = entity.getOrderRows().stream()
                    .sorted(Comparator.comparing(OrderRow::getId))
                    .map(this::toRowDto)
                    .toList();

            ordersDto.setOrderRows(rows);
        }

        return ordersDto;
    }


    private OrderRowDto toRowDto(OrderRow row) {
        OrderRowDto dto = modelMapper.map(row, OrderRowDto.class);

        dto.setApplianceId(row.getAppliance().getId());
        dto.setApplianceName(row.getAppliance().getName());
        dto.setModel(row.getAppliance().getModel());
        dto.setPrice(row.getAppliance().getPrice());

        return dto;
    }

    @Override
    public OrderDetailsDto DetailsToDto(Orders orders) {
        OrderDetailsDto dto = modelMapper.map(orders, OrderDetailsDto.class);

        if (orders.getClient() != null) {
            dto.setClientName(orders.getClient().getName());
            dto.setClientEmail(orders.getClient().getEmail());
        }

        if (orders.getEmployee() != null) {
            dto.setEmployeeName(orders.getEmployee().getName());
            dto.setEmployeeEmail(orders.getEmployee().getEmail());
            dto.setDepartment(orders.getEmployee().getDepartment());
        }

        if (orders.getOrderRows() != null) {
            List<OrderRowDetailsDto> rows = orders.getOrderRows().stream()
                    .map(this::detailsToRowDto).toList();

            dto.setOrderRowDetails(rows);
        }

        return dto;
    }

    private OrderRowDetailsDto detailsToRowDto(OrderRow row) {
        OrderRowDetailsDto dto = modelMapper.map(row, OrderRowDetailsDto.class);

        dto.setApplianceId(row.getAppliance().getId());
        dto.setApplianceName(row.getAppliance().getName());
        dto.setModel(row.getAppliance().getModel());
        dto.setPrice(row.getAppliance().getPrice());

        return dto;
    }
}

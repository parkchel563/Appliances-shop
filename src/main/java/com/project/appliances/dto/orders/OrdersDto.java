package com.project.appliances.dto.orders;

import com.project.appliances.dto.orderRow.OrderRowDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrdersDto {
    private Long id;

    private List<OrderRowDto> orderRows;

    @NotBlank(message = "{validation.totalPrice.positive}")
    private BigDecimal total;
}

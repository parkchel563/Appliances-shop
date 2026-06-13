package com.project.appliances.dto.orders;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrdersAdminDto {
    private Long id;

    private String clientName;
    private String clientEmail;

    private String status;
    private BigDecimal total;

    private Integer quantity;
}

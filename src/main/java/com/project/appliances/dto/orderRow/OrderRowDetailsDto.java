package com.project.appliances.dto.orderRow;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRowDetailsDto {
    private Long id;

    private Long applianceId;
    private String applianceName;
    private String model;

    private Long quantity;

    private BigDecimal price;
    private BigDecimal total;
}
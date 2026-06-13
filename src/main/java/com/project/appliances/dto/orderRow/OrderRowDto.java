package com.project.appliances.dto.orderRow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRowDto {
    private Long id;

    @NotBlank
    private Long applianceId;
    @NotBlank
    private String applianceName;
    @NotBlank
    private String model;

    @Positive(message = "{validation.quantity.positive}")
    private Long quantity;
    @Positive(message = "{validation.price.positive}")
    private BigDecimal price;
    @Positive(message = "{validation.totalPrice.positive}")
    private BigDecimal total;
}

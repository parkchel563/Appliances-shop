package com.project.appliances.dto.appliance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplianceCustomerDetailsDto {
    private Long id;
    private String name;
    private String model;
    private String category;
    private String powerType;
    private Integer power;
    private String manufacturerName;
    private Long manufacturerId;
    private BigDecimal price;
    private String description;
    private String characteristic;
}

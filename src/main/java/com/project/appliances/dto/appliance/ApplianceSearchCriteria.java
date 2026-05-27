package com.project.appliances.dto.appliance;

import com.project.appliances.model.Category;
import com.project.appliances.model.PowerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplianceSearchCriteria {
    private String name;
    private String model;
    private Category category;
    private Long manufacturerId;
    private PowerType powerType;
    private Integer minPower;
    private Integer maxPower;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
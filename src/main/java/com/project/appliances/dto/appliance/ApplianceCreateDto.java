package com.project.appliances.dto.appliance;

import com.project.appliances.model.Category;
import com.project.appliances.model.PowerType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplianceCreateDto {
    @NotBlank(message = "{validation.appliance.name.notBlank}")
    @Size(min = 2, max = 255, message = "{validation.appliance.name.size}")
    private String name;

    @NotNull(message = "{validation.appliance.category.notNull}")
    private Category category;

    @NotBlank(message = "{validation.appliance.model.notBlank}")
    @NotBlank(message = "{validation.appliance.model.notBlank}")
    @Size(min = 2, max = 255, message = "{validation.appliance.model.size}")
    private String model;

    @NotNull(message = "{validation.appliance.manufacturer.notNull}")
    private Long manufacturerId;

    @NotNull(message = "{validation.appliance.powerType.notNull}")
    private PowerType powerType;

    @NotBlank(message = "{validation.appliance.characteristic.notBlank}")
    @Size(min = 5, max = 1000, message = "{validation.appliance.characteristic.size}")
    private String characteristic;

    @NotBlank(message = "{validation.appliance.description.notBlank}")
    @Size(min = 10, max = 2000, message = "{validation.appliance.description.size}")
    private String description;

    @NotNull(message = "{validation.appliance.power.notNull}")
    @Positive(message = "{validation.appliance.power.positive}")
    private Integer power;

    @NotNull(message = "{validation.appliance.price.notNull}")
    @DecimalMin(value = "0.01", message = "{validation.appliance.price.positive}")
    private BigDecimal price;
}


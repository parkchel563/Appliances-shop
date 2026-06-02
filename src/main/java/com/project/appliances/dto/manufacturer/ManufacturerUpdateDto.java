package com.project.appliances.dto.manufacturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManufacturerUpdateDto {
    @NotBlank(message = "{validation.name.notBlank}")
    @Size(min = 3, max = 20, message = "{validation.manufacturerName.size}")
    private String name;
}

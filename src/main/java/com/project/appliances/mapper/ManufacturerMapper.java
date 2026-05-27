package com.project.appliances.mapper;

import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.model.Manufacturer;

public interface ManufacturerMapper {
    ManufacturerDto toDto(Manufacturer manufacturer);
}

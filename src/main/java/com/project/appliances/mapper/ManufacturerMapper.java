package com.project.appliances.mapper;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.model.Manufacturer;

public interface ManufacturerMapper {
    ManufacturerDto toDto(Manufacturer manufacturer);

    Manufacturer toCreateEntity(ManufacturerCreateDto dto);

    Manufacturer toUpdateEntity(ManufacturerUpdateDto dto, Manufacturer manufacturer);

    ManufacturerUpdateDto updateToDto(Manufacturer manufacturer);
}

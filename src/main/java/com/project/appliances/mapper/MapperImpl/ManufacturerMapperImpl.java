package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.mapper.ManufacturerMapper;
import com.project.appliances.model.Manufacturer;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManufacturerMapperImpl implements ManufacturerMapper {

    private final ModelMapper modelMapper;

    @Override
    public ManufacturerDto toDto(Manufacturer manufacturer) {
        return modelMapper.map(manufacturer, ManufacturerDto.class);
    }

    @Override
    public Manufacturer toCreateEntity(ManufacturerCreateDto dto) {
        return modelMapper.map(dto, Manufacturer.class);
    }

    @Override
    public Manufacturer toUpdateEntity(ManufacturerUpdateDto dto, Manufacturer manufacturer) {
        modelMapper.map(dto, manufacturer);
        return manufacturer;
    }

    @Override
    public ManufacturerUpdateDto updateToDto(Manufacturer manufacturer) {
        return modelMapper.map(manufacturer, ManufacturerUpdateDto.class);
    }
}
package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceCustomerDetailsDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.mapper.ApplianceMapper;
import com.project.appliances.model.Appliance;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplianceMapperImpl implements ApplianceMapper {

    private final ModelMapper modelMapper;

    @Override
    public ApplianceDto toDto(Appliance appliance) {
        return modelMapper.map(appliance, ApplianceDto.class);
    }

    @Override
    public ApplianceCreateDto toCreateDto(Appliance appliance) {
        return modelMapper.map(appliance, ApplianceCreateDto.class);
    }

    @Override
    public Appliance toEntity(ApplianceCreateDto dto) {
        Appliance appliance = new Appliance();
        appliance.setName(dto.getName());
        appliance.setCategory(dto.getCategory());
        appliance.setModel(dto.getModel());
        appliance.setPowerType(dto.getPowerType());
        appliance.setCharacteristic(dto.getCharacteristic());
        appliance.setDescription(dto.getDescription());
        appliance.setPower(dto.getPower());
        appliance.setPrice(dto.getPrice());
        return appliance;
    }

    @Override
    public ApplianceUpdateDto toUpdateDto(Appliance appliance) {
        ApplianceUpdateDto dto = modelMapper.map(appliance, ApplianceUpdateDto.class);
        dto.setManufacturerId(appliance.getManufacturer().getId());
        return dto;
    }

    @Override
    public Appliance updateToEntity(ApplianceUpdateDto dto, Appliance appliance) {
        appliance.setName(dto.getName());
        appliance.setCategory(dto.getCategory());
        appliance.setModel(dto.getModel());
        appliance.setPowerType(dto.getPowerType());
        appliance.setCharacteristic(dto.getCharacteristic());
        appliance.setDescription(dto.getDescription());
        appliance.setPower(dto.getPower());
        appliance.setPrice(dto.getPrice());
        return appliance;
    }

    @Override
    public ApplianceCustomerDetailsDto toCustomerDetailsDto(Appliance appliance) {
        ApplianceCustomerDetailsDto dto = modelMapper.map(appliance, ApplianceCustomerDetailsDto.class);
        if (appliance.getManufacturer() != null) {
            dto.setManufacturerName(appliance.getManufacturer().getName());
            dto.setManufacturerId(appliance.getManufacturer().getId());
        }
        return dto;
    }


}



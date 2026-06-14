package com.project.appliances.mapper;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceCustomerDetailsDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.model.Appliance;

public interface ApplianceMapper {
    ApplianceDto toDto(Appliance appliance);

    ApplianceCreateDto toCreateDto(Appliance appliance);

    Appliance toEntity(ApplianceCreateDto dto);

    ApplianceUpdateDto toUpdateDto(Appliance appliance);

    Appliance updateToEntity(ApplianceUpdateDto dto, Appliance appliance);

    ApplianceCustomerDetailsDto toCustomerDetailsDto(Appliance appliance);
}


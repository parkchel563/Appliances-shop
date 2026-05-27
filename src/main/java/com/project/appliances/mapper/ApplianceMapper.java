package com.project.appliances.mapper;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.model.Appliance;

public interface ApplianceMapper {
    ApplianceDto toDto(Appliance appliance);
}

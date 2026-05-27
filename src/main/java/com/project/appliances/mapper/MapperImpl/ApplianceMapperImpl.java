package com.project.appliances.mapper.MapperImpl;

import com.project.appliances.dto.appliance.ApplianceDto;
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
}


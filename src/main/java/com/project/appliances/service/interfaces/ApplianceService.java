package com.project.appliances.service.interfaces;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplianceService {
    List<ApplianceDto> findAll();

    void createAppliance(ApplianceCreateDto dto);

    ApplianceUpdateDto getApplianceDetails(Long id);

    void updateAppliance(ApplianceUpdateDto dto, Long id);

    void deleteAppliance(Long id);

    Page<ApplianceDto> findAll(ApplianceSearchCriteria criteria, Pageable pageable);
}

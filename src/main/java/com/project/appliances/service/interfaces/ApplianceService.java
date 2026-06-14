package com.project.appliances.service.interfaces;

import com.project.appliances.dto.appliance.*;
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

    ApplianceCustomerDetailsDto getCustomerApplianceDetails(Long id);

    List<ApplianceDto> getSimilarAppliances(Long id);
}

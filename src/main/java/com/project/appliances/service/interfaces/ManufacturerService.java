package com.project.appliances.service.interfaces;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManufacturerService {
    List<ManufacturerDto> findAll();
}


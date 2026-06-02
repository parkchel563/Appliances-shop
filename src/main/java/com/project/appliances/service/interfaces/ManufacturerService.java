package com.project.appliances.service.interfaces;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManufacturerService {
    Page<ManufacturerDto> findAll(ManufacturerSearchCriteria criteria, Pageable pageable);

    List<ManufacturerDto> findAll();

    void createManufacturer(ManufacturerCreateDto dto);

    ManufacturerUpdateDto getManufacturerDetails(Long id);

    void updateManufacturer(ManufacturerUpdateDto dto, Long id);

    void deleteManufacturer(Long id);
}


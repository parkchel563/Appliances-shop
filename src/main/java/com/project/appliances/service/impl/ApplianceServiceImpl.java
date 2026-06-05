package com.project.appliances.service.impl;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.exception.ApplianceNotFoundException;
import com.project.appliances.exception.ManufacturerNotFoundException;
import com.project.appliances.mapper.ApplianceMapper;
import com.project.appliances.model.Appliance;
import com.project.appliances.model.Manufacturer;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ManufacturerRepository;
import com.project.appliances.repository.specification.ApplianceSpecification;
import com.project.appliances.service.interfaces.ApplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplianceServiceImpl implements ApplianceService {

    private final ApplianceRepository applianceRepository;
    private final ApplianceMapper applianceMapper;
    private final ManufacturerRepository manufacturerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ApplianceDto> findAll() {
        return applianceRepository.findAll()
                .stream().map(applianceMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplianceDto> findAll(ApplianceSearchCriteria criteria, Pageable pageable) {

        return applianceRepository.findAll(
                ApplianceSpecification.createFilterSpecification(criteria), pageable
        ).map(applianceMapper::toDto);
    }

    @Override
    @Transactional
    public void createAppliance(ApplianceCreateDto dto) {
        Manufacturer manufacturer = manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ManufacturerNotFoundException(dto.getManufacturerId()));

        Appliance appliance = applianceMapper.toEntity(dto);

        appliance.setManufacturer(manufacturer);

        applianceRepository.save(appliance);

        log.info("BUSINESS EVENT | Appliance created | name={} manufacturerId={}",
                dto.getName(), dto.getManufacturerId());
    }

    @Override
    @Transactional
    public ApplianceUpdateDto getApplianceDetails(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(id));

        return applianceMapper.toUpdateDto(appliance);
    }

    @Override
    @Transactional
    public void updateAppliance(ApplianceUpdateDto dto, Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(id));

        Manufacturer manufacturer = manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ManufacturerNotFoundException(dto.getManufacturerId()));

        applianceMapper.updateToEntity(dto, appliance);
        appliance.setManufacturer(manufacturer);

        applianceRepository.save(appliance);

        log.info("BUSINESS EVENT | Appliance updated | id={} name={} manufacturerId={}",
                id, dto.getName(), dto.getManufacturerId());
    }

    @Override
    @Transactional
    public void deleteAppliance(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(id));

        applianceRepository.delete(appliance);
        log.info("BUSINESS EVENT | Appliance deleted | id={} name={}", id, appliance.getName());
    }
}


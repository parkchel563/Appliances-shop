package com.project.appliances.service.impl;

import com.project.appliances.dto.appliance.*;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public ApplianceCustomerDetailsDto getCustomerApplianceDetails(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(id));

        return applianceMapper.toCustomerDetailsDto(appliance);
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

    @Override
    @Transactional(readOnly = true)
    public List<ApplianceDto> getSimilarAppliances(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ApplianceNotFoundException(id));

        int limit = 3;

        // Вытаскиваем ключевое слово для поиска
        String searchKeyword = appliance.getName();
        if (searchKeyword != null && searchKeyword.contains(" ")) {
            String[] parts = searchKeyword.split(" ");
            searchKeyword = parts[parts.length - 1]; // берем последнее слово (например, "Laptop" из "Gaming Laptop")
        }

        // 1. Ищем товары, содержащие ключевое слово, от того же производителя
        List<Appliance> similarAppliances = applianceRepository.findByNameContainingIgnoreCaseAndManufacturerIdAndIdNot(
                searchKeyword,
                appliance.getManufacturer().getId(),
                id,
                PageRequest.of(0, limit)
        ).getContent();

        List<Appliance> resultList = new ArrayList<>(similarAppliances);

        // 2. Fallback: если не хватает 3 штук, добираем товары с таким же ключевым словом от других производителей
        if (resultList.size() < limit) {
            int neededCount = limit - resultList.size();

            List<Appliance> otherManufacturersAppliances = applianceRepository.findByNameContainingIgnoreCaseAndManufacturerIdNotAndIdNot(
                    searchKeyword,
                    appliance.getManufacturer().getId(),
                    id,
                    PageRequest.of(0, neededCount)
            ).getContent();

            resultList.addAll(otherManufacturersAppliances);
        }

        return resultList.stream()
                .map(applianceMapper::toDto)
                .toList();
    }
}


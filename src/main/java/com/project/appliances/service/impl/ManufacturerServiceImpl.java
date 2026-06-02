package com.project.appliances.service.impl;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.exception.ManufacturerNotFoundException;
import com.project.appliances.mapper.ManufacturerMapper;
import com.project.appliances.model.Manufacturer;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ManufacturerRepository;
import com.project.appliances.repository.specification.ManufacturerSpecification;
import com.project.appliances.service.interfaces.ManufacturerService;
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
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;
    private final ApplianceRepository applianceRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerDto> findAll() {
        return manufacturerRepository.findAll()
                .stream()
                .map(manufacturerMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerDto> findAll(ManufacturerSearchCriteria criteria, Pageable pageable) {
        return manufacturerRepository.findAll(
                        ManufacturerSpecification.createFilterSpecification(criteria), pageable)
                .map(manufacturerMapper::toDto);
    }

    @Override
    @Transactional
    public void createManufacturer(ManufacturerCreateDto dto) {
        Manufacturer manufacturer = manufacturerMapper.toCreateEntity(dto);

        if (manufacturerRepository.existsManufacturerByNameIgnoreCase(dto.getName())) {
            log.warn("BUSINESS EVENT | Manufacturer already exists | name={}", dto.getName());
            throw new IllegalArgumentException("Manufacturer with name " + dto.getName() + " already exists");
        }

        manufacturerRepository.save(manufacturer);
        log.info("BUSINESS EVENT | Manufacturer created | name={}", dto.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturerUpdateDto getManufacturerDetails(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ManufacturerNotFoundException(id));

        return manufacturerMapper.updateToDto(manufacturer);
    }

    @Override
    @Transactional
    public void updateManufacturer(ManufacturerUpdateDto dto, Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ManufacturerNotFoundException(id));

        if (manufacturerRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            log.warn("BUSINESS EVENT | Manufacturer update rejected, name already exists | id={} name={}", id, dto.getName());
            throw new IllegalArgumentException("Manufacturer already exists");
        }

        manufacturerMapper.toUpdateEntity(dto, manufacturer);
        manufacturerRepository.save(manufacturer);
        log.info("BUSINESS EVENT | Manufacturer updated | id={} name={}", id, dto.getName());
    }

    @Override
    @Transactional
    public void deleteManufacturer(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ManufacturerNotFoundException(id));

        if (applianceRepository.existsAppliancesByManufacturerId(id)) {
            log.warn("BUSINESS EVENT | Manufacturer deletion rejected, assigned to appliances | id={}", id);
            throw new IllegalStateException("Cannot delete manufacturer because it is assigned to appliances");
        }

        manufacturerRepository.delete(manufacturer);
        log.info("BUSINESS EVENT | Manufacturer deleted | id={} name={}", id, manufacturer.getName());
    }
}
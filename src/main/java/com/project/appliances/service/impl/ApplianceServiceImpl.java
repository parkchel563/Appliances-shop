package com.project.appliances.service.impl;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.mapper.ApplianceMapper;
import com.project.appliances.repository.ApplianceRepository;
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
}

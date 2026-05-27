package com.project.appliances.service.impl;

import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.mapper.ManufacturerMapper;
import com.project.appliances.repository.ManufacturerRepository;
import com.project.appliances.service.interfaces.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerDto> findAll() {
        return manufacturerRepository.findAll()
                .stream()
                .map(manufacturerMapper::toDto).toList();
    }
}

package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.mapper.ApplianceMapper;
import com.project.appliances.model.Appliance;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.service.impl.ApplianceServiceImpl;
import com.project.appliances.util.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ApplianceServiceImplTest {
    @Mock
    private ApplianceRepository applianceRepository;
    @Mock
    private ApplianceMapper applianceMapper;
    @InjectMocks
    private ApplianceServiceImpl applianceServiceImpl;

    // findAll()
    @Test
    void shouldReturnAllAppliances() {
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceDto dto = new ApplianceDto();

        when(applianceRepository.findAll()).thenReturn(List.of(appliance));
        when(applianceMapper.toDto(appliance)).thenReturn(dto);

        List<ApplianceDto> result = applianceServiceImpl.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));

        verify(applianceRepository).findAll();
        verify(applianceMapper).toDto(appliance);
    }

    @Test
    void shouldReturnPagedAppliances_whenCriteriaIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceDto dto = new ApplianceDto();
        Page<Appliance> page = new PageImpl<>(List.of(appliance));

        when(applianceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(applianceMapper.toDto(appliance)).thenReturn(dto);

        Page<ApplianceDto> result = applianceServiceImpl.findAll(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(applianceRepository).findAll(any(Specification.class), eq(pageable));
        verify(applianceMapper).toDto(appliance);
    }

}

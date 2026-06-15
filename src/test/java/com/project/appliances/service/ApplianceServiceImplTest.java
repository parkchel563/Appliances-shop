package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceCustomerDetailsDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.exception.ApplianceNotFoundException;
import com.project.appliances.exception.ManufacturerNotFoundException;
import com.project.appliances.mapper.ApplianceMapper;
import com.project.appliances.model.Appliance;
import com.project.appliances.model.Manufacturer;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ManufacturerRepository;
import com.project.appliances.service.impl.ApplianceServiceImpl;
import com.project.appliances.util.TestDataFactory;
import java.util.List;
import java.util.Optional;
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
    @Mock
    private ManufacturerRepository manufacturerRepository;
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

    // createAppliance()
    @Test
    void shouldCreateApplianceSuccessfully() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        ApplianceCreateDto dto = TestDataFactory.createApplianceCreateDto(manufacturer.getId());
        Appliance appliance = TestDataFactory.createAppliance(manufacturer);

        when(manufacturerRepository.findById(manufacturer.getId())).thenReturn(Optional.of(manufacturer));
        when(applianceMapper.toEntity(dto)).thenReturn(appliance);

        applianceServiceImpl.createAppliance(dto);

        assertEquals(manufacturer, appliance.getManufacturer());

        verify(manufacturerRepository).findById(manufacturer.getId());
        verify(applianceMapper).toEntity(dto);
        verify(applianceRepository).save(appliance);
    }

    @Test
    void shouldThrowWhenManufacturerNotFoundOnCreate() {
        ApplianceCreateDto dto = TestDataFactory.createApplianceCreateDto(1L);

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class,
                     () -> applianceServiceImpl.createAppliance(dto));

        verify(applianceMapper, never()).toEntity(any());
        verify(applianceRepository, never()).save(any());
    }

    // getApplianceDetails()
    @Test
    void shouldReturnApplianceDetails() {
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceUpdateDto dto = TestDataFactory.createApplianceUpdateDto(1L);

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));
        when(applianceMapper.toUpdateDto(appliance)).thenReturn(dto);

        ApplianceUpdateDto result = applianceServiceImpl.getApplianceDetails(1L);

        assertEquals(dto, result);

        verify(applianceRepository).findById(1L);
        verify(applianceMapper).toUpdateDto(appliance);
    }

    @Test
    void shouldThrowWhenApplianceNotFoundOnGetDetails() {
        when(applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class,
                     () -> applianceServiceImpl.getApplianceDetails(1L));

        verify(applianceMapper, never()).toUpdateDto(any());
    }

    // updateAppliance()
    @Test
    public void shouldUpdateApplianceSuccessfully() {
        Long applianceId = 1L;
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceUpdateDto dto = TestDataFactory.createApplianceUpdateDto(manufacturer.getId());

        when(applianceRepository.findById(applianceId)).thenReturn(Optional.of(appliance));
        when(manufacturerRepository.findById(manufacturer.getId())).thenReturn(Optional.of(manufacturer));

        applianceServiceImpl.updateAppliance(dto, applianceId);

        verify(applianceRepository).findById(applianceId);
        verify(manufacturerRepository).findById(manufacturer.getId());
        verify(applianceMapper).updateToEntity(dto, appliance);
        verify(applianceRepository).save(appliance);

        assertEquals(manufacturer, appliance.getManufacturer());
    }

    @Test
    void shouldThrowWhenManufacturerNotFoundOnUpdate() {
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceUpdateDto dto = TestDataFactory.createApplianceUpdateDto(1L);

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class,
                     () -> applianceServiceImpl.updateAppliance(dto, 1L));

        verify(applianceMapper, never()).updateToEntity(any(), any());
        verify(applianceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenApplianceNotFoundOnUpdate() {
        ApplianceUpdateDto dto = TestDataFactory.createApplianceUpdateDto(1L);

        when(applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class,
                     () -> applianceServiceImpl.updateAppliance(dto, 1L));

        verify(applianceMapper, never()).updateToEntity(any(), any());
        verify(applianceRepository, never()).save(any());
    }

    @Test
    void shouldDeleteApplianceSuccessfully() {
        Appliance appliance = TestDataFactory.createAppliance();

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));

        applianceServiceImpl.deleteAppliance(1L);

        verify(applianceRepository).findById(1L);
        verify(applianceRepository).delete(appliance);
    }

    @Test
    void shouldThrowWhenApplianceNotFoundOnDelete() {
        when(applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class,
                     () -> applianceServiceImpl.deleteAppliance(1L));

        verify(applianceRepository, never()).delete((Appliance) any());
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

    @Test
    void getCustomerApplianceDetails_ShouldReturnDetailsDto_WhenApplianceExists() {
        Long applianceId = 1L;
        Appliance appliance = TestDataFactory.createAppliance();
        ApplianceCustomerDetailsDto expectedDto = new ApplianceCustomerDetailsDto();

        when(applianceRepository.findById(applianceId)).thenReturn(Optional.of(appliance));
        when(applianceMapper.toCustomerDetailsDto(appliance)).thenReturn(expectedDto);

        ApplianceCustomerDetailsDto result = applianceServiceImpl.getCustomerApplianceDetails(applianceId);

        assertEquals(expectedDto, result);
        verify(applianceRepository).findById(applianceId);
        verify(applianceMapper).toCustomerDetailsDto(appliance);
    }

    @Test
    void getCustomerApplianceDetails_ShouldThrowApplianceNotFoundException_WhenNotFound() {
        Long applianceId = 1L;
        when(applianceRepository.findById(applianceId)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class,
                     () -> applianceServiceImpl.getCustomerApplianceDetails(applianceId));

        verify(applianceRepository).findById(applianceId);
        verifyNoInteractions(applianceMapper);
    }

    @Test
    void getSimilarAppliances_ShouldThrowApplianceNotFoundException_WhenNotFound() {
        Long applianceId = 1L;
        when(applianceRepository.findById(applianceId)).thenReturn(Optional.empty());

        assertThrows(ApplianceNotFoundException.class,
                     () -> applianceServiceImpl.getSimilarAppliances(applianceId));

        verifyNoMoreInteractions(applianceRepository);
    }

    @Test
    void getSimilarAppliances_ShouldReturnFromSameManufacturer_WhenEnoughFound() {
        Long targetId = 1L;
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(10L);

        Appliance targetAppliance = new Appliance();
        targetAppliance.setId(targetId);
        targetAppliance.setName("Gaming Laptop");
        targetAppliance.setManufacturer(manufacturer);

        Appliance sim1 = new Appliance();
        Appliance sim2 = new Appliance();
        Appliance sim3 = new Appliance();
        Page<Appliance> sameBrandPage = new PageImpl<>(List.of(sim1, sim2, sim3));

        ApplianceDto mockDto = new ApplianceDto();

        when(applianceRepository.findById(targetId)).thenReturn(Optional.of(targetAppliance));
        when(applianceRepository.findByNameContainingIgnoreCaseAndManufacturerIdAndIdNot(
                eq("Laptop"), eq(10L), eq(targetId), eq(PageRequest.of(0, 3))))
                .thenReturn(sameBrandPage);
        when(applianceMapper.toDto(any(Appliance.class))).thenReturn(mockDto);

        List<ApplianceDto> result = applianceServiceImpl.getSimilarAppliances(targetId);

        assertEquals(3, result.size());
        verify(applianceRepository, never()).findByNameContainingIgnoreCaseAndManufacturerIdNotAndIdNot(
                anyString(), anyLong(), anyLong(), any(Pageable.class));
        verify(applianceMapper, times(3)).toDto(any(Appliance.class));
    }

    @Test
    void getSimilarAppliances_ShouldUseFallback_WhenNotEnoughFromSameManufacturer() {
        Long targetId = 1L;
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(10L);

        Appliance targetAppliance = new Appliance();
        targetAppliance.setId(targetId);
        targetAppliance.setName("Fridge");
        targetAppliance.setManufacturer(manufacturer);

        Appliance sameBrandAppliance = new Appliance();
        Page<Appliance> sameBrandPage = new PageImpl<>(List.of(sameBrandAppliance));

        Appliance otherBrand1 = new Appliance();
        Appliance otherBrand2 = new Appliance();
        Page<Appliance> otherBrandPage = new PageImpl<>(List.of(otherBrand1, otherBrand2));

        ApplianceDto mockDto = new ApplianceDto();

        when(applianceRepository.findById(targetId)).thenReturn(Optional.of(targetAppliance));

        when(applianceRepository.findByNameContainingIgnoreCaseAndManufacturerIdAndIdNot(
                eq("Fridge"), eq(10L), eq(targetId), eq(PageRequest.of(0, 3))))
                .thenReturn(sameBrandPage);

        when(applianceRepository.findByNameContainingIgnoreCaseAndManufacturerIdNotAndIdNot(
                eq("Fridge"), eq(10L), eq(targetId), eq(PageRequest.of(0, 2))))
                .thenReturn(otherBrandPage);

        when(applianceMapper.toDto(any(Appliance.class))).thenReturn(mockDto);

        List<ApplianceDto> result = applianceServiceImpl.getSimilarAppliances(targetId);

        assertEquals(3, result.size());
        verify(applianceRepository).findByNameContainingIgnoreCaseAndManufacturerIdAndIdNot("Fridge", 10L, targetId,
                                                                                            PageRequest.of(0, 3));
        verify(applianceRepository).findByNameContainingIgnoreCaseAndManufacturerIdNotAndIdNot("Fridge", 10L, targetId,
                                                                                               PageRequest.of(0, 2));
        verify(applianceMapper, times(3)).toDto(any(Appliance.class));
    }

}


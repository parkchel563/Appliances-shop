package com.project.appliances.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.exception.ManufacturerNotFoundException;
import com.project.appliances.mapper.ManufacturerMapper;
import com.project.appliances.model.Manufacturer;
import com.project.appliances.repository.ApplianceRepository;
import com.project.appliances.repository.ManufacturerRepository;
import com.project.appliances.service.impl.ManufacturerServiceImpl;
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
class ManufacturerServiceImplTest {
    private static final Long MANUFACTURER_ID = 1L;
    private static final String MANUFACTURER_NAME = "Dyson";
    @Mock
    private ManufacturerRepository manufacturerRepository;
    @Mock
    private ManufacturerMapper manufacturerMapper;
    @Mock
    private ApplianceRepository applianceRepository;
    @InjectMocks
    private ManufacturerServiceImpl manufacturerService;

    // findAll() & Pagination
    @Test
    void shouldReturnAllManufacturers() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        ManufacturerDto dto = new ManufacturerDto();

        when(manufacturerRepository.findAll()).thenReturn(List.of(manufacturer));
        when(manufacturerMapper.toDto(manufacturer)).thenReturn(dto);

        List<ManufacturerDto> result = manufacturerService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void shouldReturnPagedManufacturers() {
        ManufacturerSearchCriteria criteria = new ManufacturerSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        ManufacturerDto dto = new ManufacturerDto();
        Page<Manufacturer> page = new PageImpl<>(List.of(manufacturer));

        when(manufacturerRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(manufacturerMapper.toDto(manufacturer)).thenReturn(dto);

        Page<ManufacturerDto> result = manufacturerService.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        verify(manufacturerRepository).findAll(any(Specification.class), eq(pageable));
    }

    // createManufacturer
    @Test
    void shouldCreateManufacturerSuccessfully() {
        ManufacturerCreateDto dto = TestDataFactory.createManufacturerCreateDto(MANUFACTURER_NAME);
        Manufacturer manufacturer = TestDataFactory.createManufacturer();

        when(manufacturerMapper.toCreateEntity(dto)).thenReturn(manufacturer);
        when(manufacturerRepository.existsManufacturerByNameIgnoreCase(MANUFACTURER_NAME)).thenReturn(false);

        manufacturerService.createManufacturer(dto);

        verify(manufacturerRepository).save(manufacturer);
    }

    @Test
    void shouldThrowException_whenCreatingAndNameAlreadyExists() {
        ManufacturerCreateDto dto = TestDataFactory.createManufacturerCreateDto(MANUFACTURER_NAME);
        Manufacturer manufacturer = TestDataFactory.createManufacturer();

        when(manufacturerMapper.toCreateEntity(dto)).thenReturn(manufacturer);
        when(manufacturerRepository.existsManufacturerByNameIgnoreCase(MANUFACTURER_NAME)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> manufacturerService.createManufacturer(dto));

        assertEquals("Manufacturer with name " + MANUFACTURER_NAME + " already exists", ex.getMessage());
        verify(manufacturerRepository, never()).save(manufacturer);
    }

    // getManufacturerDetails()
    @Test
    void shouldReturnManufacturerDetails() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        ManufacturerUpdateDto dto = TestDataFactory.createManufacturerUpdateDto(MANUFACTURER_NAME);

        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.of(manufacturer));
        when(manufacturerMapper.updateToDto(manufacturer)).thenReturn(dto);

        ManufacturerUpdateDto result = manufacturerService.getManufacturerDetails(MANUFACTURER_ID);

        assertEquals(dto, result);
    }

    @Test
    void shouldThrowException_whenManufacturerNotFoundById() {
        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class,
                     () -> manufacturerService.getManufacturerDetails(MANUFACTURER_ID));
    }

    // updateManufacturer()
    @Test
    void shouldUpdateManufacturerSuccessfully() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        String newName = "Samsung";
        ManufacturerUpdateDto dto = TestDataFactory.createManufacturerUpdateDto(newName);

        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.of(manufacturer));
        when(manufacturerRepository.existsByNameIgnoreCaseAndIdNot(newName, MANUFACTURER_ID)).thenReturn(false);

        manufacturerService.updateManufacturer(dto, MANUFACTURER_ID);

        verify(manufacturerMapper).toUpdateEntity(dto, manufacturer);
        verify(manufacturerRepository).save(manufacturer);
    }

    @Test
    void shouldThrowException_whenUpdatingAndNameExistsForAnotherId() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();
        String newName = "Samsung";
        ManufacturerUpdateDto dto = TestDataFactory.createManufacturerUpdateDto(newName);

        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.of(manufacturer));
        when(manufacturerRepository.existsByNameIgnoreCaseAndIdNot(newName, MANUFACTURER_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> manufacturerService.updateManufacturer(dto, MANUFACTURER_ID));

        assertEquals("Manufacturer already exists", ex.getMessage());
        verify(manufacturerMapper, never()).toUpdateEntity(dto, manufacturer);
        verify(manufacturerRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenManufacturerNotFoundOnUpdate() {
        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class,
                     () -> manufacturerService.updateManufacturer(new ManufacturerUpdateDto(), MANUFACTURER_ID));
    }

    // deleteManufacturer
    @Test
    void shouldDeleteManufacturerSuccessfully() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();

        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.of(manufacturer));
        when(applianceRepository.existsAppliancesByManufacturerId(MANUFACTURER_ID)).thenReturn(false);

        manufacturerService.deleteManufacturer(MANUFACTURER_ID);

        verify(manufacturerRepository).delete(manufacturer);
    }

    @Test
    void shouldThrowException_whenDeletingManufacturerWithAssignedAppliances() {
        Manufacturer manufacturer = TestDataFactory.createManufacturer();

        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.of(manufacturer));
        when(applianceRepository.existsAppliancesByManufacturerId(MANUFACTURER_ID)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                                                () -> manufacturerService.deleteManufacturer(MANUFACTURER_ID));

        assertEquals("Cannot delete manufacturer because it is assigned to appliances", ex.getMessage());
        verify(manufacturerRepository, never()).delete(manufacturer);
    }

    @Test
    void shouldThrowException_whenManufacturerNotFoundOnDelete() {
        when(manufacturerRepository.findById(MANUFACTURER_ID)).thenReturn(Optional.empty());

        assertThrows(ManufacturerNotFoundException.class,
                     () -> manufacturerService.deleteManufacturer(MANUFACTURER_ID));
    }
}

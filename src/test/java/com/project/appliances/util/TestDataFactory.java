package com.project.appliances.util;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.model.Appliance;
import com.project.appliances.model.Client;
import com.project.appliances.model.Employee;
import com.project.appliances.model.Manufacturer;
import java.math.BigDecimal;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Manufacturer createManufacturer() {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(1L);
        manufacturer.setName("Dyson");
        return manufacturer;
    }

    public static Appliance createAppliance() {
        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setName("Hair Dryer");
        appliance.setManufacturer(createManufacturer());
        appliance.setPrice(BigDecimal.valueOf(100));
        return appliance;
    }

    public static Appliance createAppliance(Manufacturer manufacturer) {
        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setName("Hair Dryer");
        appliance.setManufacturer(manufacturer);
        appliance.setPrice(BigDecimal.valueOf(100));
        return appliance;
    }

    public static ApplianceCreateDto createApplianceCreateDto(Long manufacturerId) {
        ApplianceCreateDto dto = new ApplianceCreateDto();
        dto.setName("Refrigerator");
        dto.setManufacturerId(manufacturerId);
        dto.setPrice(BigDecimal.valueOf(1000));
        return dto;
    }

    public static ApplianceUpdateDto createApplianceUpdateDto(Long manufacturerId) {
        ApplianceUpdateDto dto = new ApplianceUpdateDto();
        dto.setName("Updated Appliance");
        dto.setManufacturerId(manufacturerId);
        dto.setPrice(BigDecimal.valueOf(200));
        return dto;
    }

    public static Client createClient(Long id, String email) {
        Client client = new Client();
        client.setId(id);
        client.setEmail(email);
        client.setName("Test User");
        return client;
    }


    public static Employee createEmployee(Long id, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmail(email);
        employee.setName("Test Employee");
        return employee;
    }

    public static ManufacturerCreateDto createManufacturerCreateDto(String name) {
        ManufacturerCreateDto dto = new ManufacturerCreateDto();
        dto.setName(name);
        return dto;
    }

    public static ManufacturerUpdateDto createManufacturerUpdateDto(String name) {
        ManufacturerUpdateDto dto = new ManufacturerUpdateDto();
        dto.setName(name);
        return dto;
    }

}

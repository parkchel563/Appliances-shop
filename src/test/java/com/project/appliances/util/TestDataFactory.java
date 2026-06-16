package com.project.appliances.util;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.model.*;
import java.math.BigDecimal;
import java.util.HashSet;

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

    public static Orders createTestCart() {
        Orders cart = new Orders();
        cart.setId(10L);
        cart.setStatus(OrderStatus.NEW);
        cart.setOrderRows(new HashSet<>());
        cart.setTotal(BigDecimal.ZERO);
        return cart;
    }

    public static OrderRow createOrderRow(Long id, BigDecimal price) {
        Appliance appliance = createAppliance();
        appliance.setPrice(price);

        OrderRow row = new OrderRow();
        row.setId(id);
        row.setAppliance(appliance);
        row.setQuantity(1L);
        row.setTotal(price);
        return row;
    }

    public static Client createClient(Long id, String email) {
        Client client = new Client();
        client.setId(id);
        client.setEmail(email);
        client.setName("Test User");
        return client;
    }

    public static ClientUpdateProfileDto createClientUpdateDto(String email) {
        ClientUpdateProfileDto dto = new ClientUpdateProfileDto();
        dto.setEmail(email);
        dto.setName("Updated User");
        return dto;
    }

    public static Employee createEmployee(Long id, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmail(email);
        employee.setName("Test Employee");
        return employee;
    }

    public static EmployeeUpdateProfileDto createEmployeeUpdateDto(String email) {
        EmployeeUpdateProfileDto dto = new EmployeeUpdateProfileDto();
        dto.setEmail(email);
        dto.setName("Updated Employee");
        return dto;
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

    public static Orders createOrder(Long id, OrderStatus status) {
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

}

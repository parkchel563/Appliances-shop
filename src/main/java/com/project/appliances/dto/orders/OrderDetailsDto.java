package com.project.appliances.dto.orders;

import com.project.appliances.dto.orderRow.OrderRowDetailsDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailsDto {
    private Long id;

    private String clientName;
    private String clientEmail;

    private String employeeName;
    private String employeeEmail;
    private String department;

    private List<OrderRowDetailsDto> orderRowDetails;

    private String status;
    private BigDecimal total;

}

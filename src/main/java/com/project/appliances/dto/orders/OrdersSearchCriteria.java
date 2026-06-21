package com.project.appliances.dto.orders;

import com.project.appliances.model.OrderStatus;
import lombok.Data;

@Data
public class OrdersSearchCriteria {
    private String id;
    private String employeeName;
    private String clientName;
    private OrderStatus status;
}

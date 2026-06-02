package com.project.appliances.exception;

import com.project.appliances.constants.MessageKeys;

public class OrderNotFoundException extends ResourceNotFoundException {
    public OrderNotFoundException(Long id) {
        super(MessageKeys.ORDER_NOT_FOUND, id);
    }
}


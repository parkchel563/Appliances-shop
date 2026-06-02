package com.project.appliances.exception;

import com.project.appliances.constants.MessageKeys;

public class ManufacturerNotFoundException extends ResourceNotFoundException {
    public ManufacturerNotFoundException(Long id) {
        super(MessageKeys.MANUFACTURER_NOT_FOUND, id);
    }
}

package com.project.appliances.exception;

import com.project.appliances.constants.MessageKeys;

public class ApplianceNotFoundException extends ResourceNotFoundException {

    public ApplianceNotFoundException(long id) {
        super(MessageKeys.APPLIANCE_NOT_FOUND, id);
    }
}

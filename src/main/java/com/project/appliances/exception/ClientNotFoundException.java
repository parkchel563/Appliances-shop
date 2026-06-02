package com.project.appliances.exception;

import com.project.appliances.constants.MessageKeys;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(Long id) {
        super(MessageKeys.CLIENT_NOT_FOUND_BY_ID, id);
    }

    public ClientNotFoundException(String email) {
        super(MessageKeys.CLIENT_NOT_FOUND_BY_EMAIL, email);
    }

}

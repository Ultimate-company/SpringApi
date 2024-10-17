package com.example.SpringApi.Authentication;

public class PermissionException extends RuntimeException {

    public PermissionException(String message) {
        super(message);
    }
}

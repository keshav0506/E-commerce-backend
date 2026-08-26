package com.keshav.exception;

public class AddressNotFoundException
        extends RuntimeException {

    public AddressNotFoundException(String message) {
        super(message);
    }
}
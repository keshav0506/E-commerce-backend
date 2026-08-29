package com.keshav.entity;

public enum Role {
    CUSTOMER,
    SUPPLIER,
    ADMIN;

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return CUSTOMER;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER;
        }
    }
}

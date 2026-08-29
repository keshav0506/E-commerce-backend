package com.keshav.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierApplyRequestDTO {

    @NotBlank(message = "Contact person name is required")
    private String name;

    @NotBlank(message = "Login email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid business email")
    private String businessEmail;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    private String city;
    private String state;
    private String postalCode;
    private String country = "India";

    @NotBlank(message = "Tax Identifier (GSTIN/PAN/Tax ID) is required")
    private String taxIdentifier;

    private String category = "General Merchandise";
}

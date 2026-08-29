package com.keshav.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequestDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits")
    private String phone;

    @NotBlank
    private String addressLine;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$",
            message = "Postal code must contain exactly 6 digits")
    private String postalCode;

    @NotBlank
    private String country;

    private boolean isDefault;
}
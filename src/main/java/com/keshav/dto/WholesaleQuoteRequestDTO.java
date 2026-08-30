package com.keshav.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WholesaleQuoteRequestDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    private String contactPhone;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String notes;

    /** Optional: product ID the buyer was viewing when requesting the quote */
    private Long productId;

    /** Optional: product name for context */
    private String productName;
}

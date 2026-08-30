package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WholesaleQuoteResponseDTO {
    private Long id;
    private String referenceId;
    private String supplierBusinessName;
    private String companyName;
    private String contactName;
    private String contactEmail;
    private int quantity;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}

package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSummaryDTO {
    private Long id;
    private String businessName;
    private String businessEmail;
    private String category;
    private String city;
    private String state;
    private String status;
}

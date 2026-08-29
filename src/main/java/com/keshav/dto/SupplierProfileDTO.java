package com.keshav.dto;

import com.keshav.entity.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfileDTO {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String businessName;
    private String businessEmail;
    private String phone;
    private String businessAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String taxIdentifier;
    private String category;
    private SupplierStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

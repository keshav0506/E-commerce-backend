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
public class SupplierNotificationDTO {

    private Long id;
    private Long supplierId;
    private String title;
    private String message;
    private String type;
    private String targetUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}

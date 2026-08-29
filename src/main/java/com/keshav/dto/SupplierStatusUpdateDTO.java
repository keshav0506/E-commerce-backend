package com.keshav.dto;

import com.keshav.entity.SupplierStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private SupplierStatus status;

    private String reason;
}

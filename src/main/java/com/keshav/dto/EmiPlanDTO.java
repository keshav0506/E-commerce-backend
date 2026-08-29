package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiPlanDTO {
    private double productPrice;
    private double minEmiAmount;
    private String bestTenureText;
    private List<EmiOptionDTO> plans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmiOptionDTO {
        private String bankName;
        private String cardType;
        private int tenureMonths;
        private double interestRate;
        private double monthlyInstallment;
        private double totalPayable;
        private double processingFee;
        private boolean isNoCost;
    }
}

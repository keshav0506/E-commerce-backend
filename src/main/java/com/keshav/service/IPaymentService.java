package com.keshav.service;

import com.keshav.dto.PaymentOrderResponseDTO;
import com.keshav.dto.PaymentVerificationRequestDTO;

public interface IPaymentService {

    PaymentOrderResponseDTO createPaymentOrder(
            Long orderId
    );

    void verifyPayment(
            PaymentVerificationRequestDTO request
    );
}
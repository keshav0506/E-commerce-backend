package com.keshav.controller;

import com.keshav.dto.PaymentOrderResponseDTO;
import com.keshav.dto.PaymentVerificationRequestDTO;
import com.keshav.service.IPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(
            IPaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping("/create/{orderId}")
    public ResponseEntity<PaymentOrderResponseDTO>
    createPaymentOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.createPaymentOrder(
                        orderId
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @Valid
            @RequestBody PaymentVerificationRequestDTO request) {

        paymentService.verifyPayment(request);

        return ResponseEntity.ok(
                "Payment verified successfully"
        );
    }
}
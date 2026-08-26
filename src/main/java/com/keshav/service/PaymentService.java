package com.keshav.service;

import com.keshav.dto.PaymentOrderResponseDTO;
import com.keshav.dto.PaymentVerificationRequestDTO;
import com.keshav.entity.Order;
import com.keshav.entity.OrderStatus;
import com.keshav.entity.Payment;
import com.keshav.entity.PaymentStatus;
import com.keshav.exception.OrderNotFoundException;
import com.keshav.repository.OrderRepository;
import com.keshav.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService implements IPaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentService(
            RazorpayClient razorpayClient,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {

        this.razorpayClient = razorpayClient;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentOrderResponseDTO createPaymentOrder(
            Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        if (order.getStatus()
                != OrderStatus.PENDING_PAYMENT) {

            throw new IllegalStateException(
                    "Payment cannot be created for this order"
            );
        }

        try {

            long amountInPaise =
                    Math.round(
                            order.getTotalAmount() * 100
                    );

            JSONObject options = new JSONObject();

            options.put(
                    "amount",
                    amountInPaise
            );

            options.put(
                    "currency",
                    "INR"
            );

            options.put(
                    "receipt",
                    "order_" + order.getId()
            );

            com.razorpay.Order razorpayOrder =
                    razorpayClient.orders.create(options);

            String razorpayOrderId =
                    razorpayOrder.get("id");

            // Create our Payment record
            Payment payment = new Payment();

            payment.setOrder(order);

            payment.setAmount(
                    order.getTotalAmount()
            );

            payment.setRazorpayOrderId(
                    razorpayOrderId
            );

            payment.setStatus(
                    PaymentStatus.CREATED
            );

            payment.setCreatedAt(
                    LocalDateTime.now()
            );

            paymentRepository.save(payment);

            return new PaymentOrderResponseDTO(
                    razorpayOrderId,
                    keyId,
                    order.getId(),
                    order.getTotalAmount()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to create payment order",
                    e
            );
        }
    }

    @Override
    @Transactional
    public void verifyPayment(
            PaymentVerificationRequestDTO request) {

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                request.getRazorpayOrderId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment record not found"
                                )
                        );

        try {

            JSONObject options =
                    new JSONObject();

            options.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId()
            );

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );

            boolean verified =
                    Utils.verifyPaymentSignature(
                            options,
                            keySecret
                    );

            if (!verified) {

                payment.setStatus(
                        PaymentStatus.FAILED
                );

                paymentRepository.save(payment);

                throw new IllegalStateException(
                        "Payment verification failed"
                );
            }

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            payment.setRazorpaySignature(
                    request.getRazorpaySignature()
            );

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );

            paymentRepository.save(payment);

            // Confirm our order
            Order order = payment.getOrder();

            order.setStatus(
                    OrderStatus.CONFIRMED
            );

            orderRepository.save(order);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Payment verification failed",
                    e
            );
        }
    }
}
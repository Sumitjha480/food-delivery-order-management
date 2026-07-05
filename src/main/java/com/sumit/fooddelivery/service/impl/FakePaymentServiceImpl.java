package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.PaymentRequest;
import com.sumit.fooddelivery.payment.PaymentResult;
import com.sumit.fooddelivery.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class FakePaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResult charge(BigDecimal amount, PaymentRequest paymentRequest) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentResult.failure("Invalid payment amount");
        }

        if (paymentRequest == null) {
            return PaymentResult.failure("Payment details are missing");
        }

        String token = paymentRequest.getToken();

        if ("FAIL".equalsIgnoreCase(token)
                || "DECLINE".equalsIgnoreCase(token)
                || "INVALID".equalsIgnoreCase(token)) {
            return PaymentResult.failure("Payment was declined by mock payment gateway");
        }

        return PaymentResult.success("PAY-" + UUID.randomUUID());
    }
}

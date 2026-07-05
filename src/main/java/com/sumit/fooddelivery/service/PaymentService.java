package com.sumit.fooddelivery.service;

import com.sumit.fooddelivery.dto.request.PaymentRequest;
import com.sumit.fooddelivery.payment.PaymentResult;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentResult charge(BigDecimal amount, PaymentRequest paymentRequest);
}

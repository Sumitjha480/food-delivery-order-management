package com.sumit.fooddelivery.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResult {

    private boolean success;

    private String reference;

    private String failureReason;

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult failure(String failureReason) {
        return new PaymentResult(false, null, failureReason);
    }
}

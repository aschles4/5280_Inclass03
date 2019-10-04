package com.fullstackdevelopers.inclass03.dto;

import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.OptionalInt;

public class CreateSaleRequest {
    PaymentMethodNonce paymentMethodNonce;
    String amount;
    Options options;

    public CreateSaleRequest(PaymentMethodNonce paymentMethodNonce, String amount, Options options) {
        this.paymentMethodNonce = paymentMethodNonce;
        this.amount = amount;
        this.options = options;
    }

    public PaymentMethodNonce getPaymentMethodNonce() {
        return paymentMethodNonce;
    }

    public void setPaymentMethodNonce(PaymentMethodNonce paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "CreateSaleRequest{" +
                "paymentMethodNonce=" + paymentMethodNonce +
                ", amount='" + amount + '\'' +
                ", options=" + options +
                '}';
    }
}

package com.fullstackdevelopers.inclass03.dto;

import com.braintreepayments.api.models.PaymentMethodNonce;

public class UpdateCustomerRequest {
    String id;
    PaymentMethodNonce paymentMethodNonce;

    public UpdateCustomerRequest(String id, PaymentMethodNonce paymentMethodNonce) {
        this.id = id;
        this.paymentMethodNonce = paymentMethodNonce;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentMethodNonce getPaymentMethodNonce() {
        return paymentMethodNonce;
    }

    public void setPaymentMethodNonce(PaymentMethodNonce paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    @Override
    public String toString() {
        return "UpdateCustomerRequest{" +
                "id='" + id + '\'' +
                ", paymentMethodNonce=" + paymentMethodNonce +
                '}';
    }
}

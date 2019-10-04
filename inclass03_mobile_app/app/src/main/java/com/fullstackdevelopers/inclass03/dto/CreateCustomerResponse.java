package com.fullstackdevelopers.inclass03.dto;

import com.google.gson.JsonObject;

public class CreateCustomerResponse {
    JsonObject customer;
    JsonObject errors;
    String message;
    Boolean success;

    public JsonObject getCustomer() {
        return customer;
    }

    public void setCustomer(JsonObject customer) {
        this.customer = customer;
    }

    public JsonObject getErrors() {
        return errors;
    }

    public void setErrors(JsonObject errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "CreateCustomerResponse{" +
                "customer=" + customer +
                ", errors=" + errors +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}

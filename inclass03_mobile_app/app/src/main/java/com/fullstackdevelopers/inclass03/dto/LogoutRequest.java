package com.fullstackdevelopers.inclass03.dto;

public class LogoutRequest {
    String token;

    public LogoutRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

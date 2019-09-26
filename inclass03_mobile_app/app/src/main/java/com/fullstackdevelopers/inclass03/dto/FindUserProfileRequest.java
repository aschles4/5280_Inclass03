package com.fullstackdevelopers.inclass03.dto;

public class FindUserProfileRequest {
    String token;

    public FindUserProfileRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

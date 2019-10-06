package com.fullstackdevelopers.inclass03.dto;

public class ClientToken {
    private String jwtToken;

    public ClientToken() {
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public String toString() {
        return "ClientToken{" +
                "jwtToken='" + jwtToken + '\'' +
                '}';
    }
}

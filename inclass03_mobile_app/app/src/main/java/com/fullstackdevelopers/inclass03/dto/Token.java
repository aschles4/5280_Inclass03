package com.fullstackdevelopers.inclass03.dto;

public class Token {
    private String clientToken;

    public Token() {
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public String toString() {
        return "Token{" +
                "clientToken='" + clientToken + '\'' +
                '}';
    }
}

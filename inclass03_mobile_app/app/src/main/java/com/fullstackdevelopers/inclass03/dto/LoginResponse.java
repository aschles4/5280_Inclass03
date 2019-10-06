package com.fullstackdevelopers.inclass03.dto;

public class LoginResponse extends ClassLoader {


    private String token;
    private String errorMsg;

    public LoginResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public LoginResponse setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}

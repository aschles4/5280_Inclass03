package com.fullstackdevelopers.inclass03.dto;

public class CreateClientTokenRequest {
    String id;

    public CreateClientTokenRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CreateClientTokenRequest{" +
                "id='" + id + '\'' +
                '}';
    }
}

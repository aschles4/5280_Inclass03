package com.fullstackdevelopers.inclass03.services;

import android.util.Log;

import com.fullstackdevelopers.inclass03.dto.FindUserProfileResponse;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InterceptResponse implements Interceptor {

    private String address;
    private String authToken;
    private FindUserProfileResponse profileResponse;
    private Gson gson = new Gson();


    public InterceptResponse(String address, String authToken, FindUserProfileResponse profileResponse) {
        this.address = address;
        this.authToken = authToken;
        this.profileResponse = profileResponse;
    }


    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder newRequest = chain.request().newBuilder();
        newRequest.url(address);
        newRequest.addHeader("Authorization", "Bearer " + authToken);
        if ( profileResponse != null ) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(profileResponse.getUserID()));
            newRequest.post(requestBody);
        }

        Log.d("INTERCEPTOR", "This is the request " + newRequest.head().toString());

        Response response = chain.proceed(newRequest.build());

        return response;
    }
}

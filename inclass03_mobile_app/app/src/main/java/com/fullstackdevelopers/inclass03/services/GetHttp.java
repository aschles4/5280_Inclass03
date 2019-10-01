package com.fullstackdevelopers.inclass03.services;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetHttp {

//    private RequestBuilder request;
    private OkHttpClient client;
    private Call c;
    private Response res;
    private GetRespListener getRespListener;

    public interface GetRespListener {
        public void r(Response res);
    }


    public GetHttp(Request request) {
        client = new OkHttpClient();
        getRespListener = null;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                getRespListener.r(response);
            }
        });
    }

    public void setGetRespListener(GetRespListener listener) {
        this.getRespListener = listener;
    }
}

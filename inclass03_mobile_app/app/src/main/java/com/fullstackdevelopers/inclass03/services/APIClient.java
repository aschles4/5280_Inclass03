package com.fullstackdevelopers.inclass03.services;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.fullstackdevelopers.inclass03.data.Product;
import com.fullstackdevelopers.inclass03.dto.ClientToken;
import com.fullstackdevelopers.inclass03.dto.LoginRequest;
import com.fullstackdevelopers.inclass03.dto.LoginResponse;
import com.fullstackdevelopers.inclass03.login.Login;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class APIClient {

    private static final String TAG = "APIClient";

    /**
     * This method logs the user into the application using their email and password
     * and returns a LoginResponse object
     * @param loginRequest
     * @return LoginResponse token
     */
    public static void loginUser(final LoginRequest loginRequest) {

        final Gson gson = new Gson();
        final Login l = new Login();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(loginRequest));

        System.out.println(requestBody.toString());
        Request request = new Request.Builder()
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/login")
                .post(requestBody)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            LoginResponse loginResponse = new LoginResponse();
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                loginResponse.setErrorMsg("Error with request");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {

                    throw new IOException("Unexpected code " + response);
                } else {
                    String resp = response.body().string();
                    loginResponse = gson.fromJson(resp,LoginResponse.class);
                    System.out.println("The response is: " + resp);
                    l.getResponse(loginResponse);
                }
            }
        });
    }

    /**
     * This method signs a user out of application
     * @param token
     */
    public static void logout(LoginResponse token) {

    }

    /**
     * This method returns a list of available store products
     * @param token
     * @return List<Product> products
     */
    public static List<Product> getProducts(LoginResponse token) {
        List<Product> products = new ArrayList<>();

        return products;
    }

    /**
     * This method returns a ClientToken from paymentProvider
     * @param token
     * @return ClientToken clientToken
     */
    public static ClientToken getClientToken(LoginResponse token) {
        ClientToken clientToken = new ClientToken();

        return clientToken;
    }

    /**
     * This method returns a boolean isCreated if customer successfully created
     * @param loginResponse
     * @return boolean isCreated
     */
    public static void createCustomer(final LoginResponse loginResponse) {
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), loginResponse.getToken());
            Log.d(TAG, "Login id " + loginResponse);

            final String url = "https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/createclient";

            Interceptor interceptor = new Interceptor() {
                @NotNull
                @Override
                public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                    Request request = chain.request();
                    Request newRequest = request.newBuilder()
                            .url(url)
                            .addHeader("Authorization","Bearer " + loginResponse.getToken())
                            .post(requestBody)
                            .build();

                    return chain.proceed(newRequest);
                }
            };
            Request request = new Request.Builder()
                    .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/createclient")
                    .addHeader("Authorization", "Bearer " + loginResponse.getToken())
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "Create client failed");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        throw new IOException("Unexpected code " + response.body().string());
//                    } else {
                        String resp = response.body().string();
                        Log.d("Response in creatClient ", resp);

//                        CreateCustomerResponse customerResponse = gson.fromJson(resp, CreateCustomerResponse.class);

//                        System.out.println(customerResponse.toString());
//                        if(!customerResponse.getSuccess()){
//                            Log.d("Response", "Customer Already Exists");
//                        }

//
//                        Intent myIntent = new Intent(getActivity(), Purchase.class);
//                        myIntent.putExtra("customerId", String.valueOf(loginResponse.getUserId()));
//                        myIntent.putExtra("authToken", loginResponse.getToken());//Optional parameters
//                        myIntent.putExtra("profile", profileResponse);
//                        getActivity().startActivity(myIntent);

                    }
//                }
            });
    }

    /**
     * This
     * @param token
     * @param amount
     * @param nOnce
     * @return
     */
    public static boolean makeSale(LoginResponse token, String amount, String nOnce) {
        boolean isSuccessful = false;
        return isSuccessful;
    }

}

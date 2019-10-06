package com.fullstackdevelopers.inclass03.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.dto.FindUserProfileResponse;
import com.fullstackdevelopers.inclass03.dto.LoginRequest;
import com.fullstackdevelopers.inclass03.dto.LoginResponse;
import com.fullstackdevelopers.inclass03.services.APIClient;
import com.google.gson.Gson;

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;


public class Login extends Fragment {
    private EditText et_email,password;
    private OkHttpClient client;
    private OnFragmentInteractionListener mListener;
    private View view;
    private Gson gson;
    private static String TAG = "Login";
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private LoginResponse loginResponse;

    public Login() {
    }


    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_email = view.findViewById(R.id.et_email);
                password = view.findViewById(R.id.et_password);
                final String e = et_email.getText().toString();
                String p = password.getText().toString();

                if (  !e.isEmpty() || e == null ) {
                    if ( !p.isEmpty() || p == null ) {
                        Log.d("login", "Inside login + creds " + e + ", " + p);

                        final LoginRequest loginRequest = new LoginRequest();
                        loginRequest.setEmail(e);
                        loginRequest.setPassword(p);
                        APIClient.loginUser(loginRequest);

                    } else { // Handle missing password
                        Toast.makeText(getContext(), R.string.empty_password, Toast.LENGTH_SHORT).show();
                    }
                } else { // Handle missing email
                    Toast.makeText(getContext(), R.string.empty_email, Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( getFragmentManager().findFragmentByTag("tag_signup") != null ) {

                    Fragment s = getFragmentManager().findFragmentByTag("tag_signup");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_layout, s, "tag_signup")
                            .commit();
                } else {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_layout, new Signup(), "tag_signup")
                            .addToBackStack("tag_login")
                            .commit();
                }
            }
        });
    }

    /**
     * This method gets the loginResponse from the APIClient
     * @param loginResponse
     */
    public void getResponse(LoginResponse loginResponse){
        Log.d(TAG,"Not sure if this works or if it is worth it if it did!: " + loginResponse);
        createCustomer(loginResponse);

    }

    /**
     * This method creates a new customer if the first call to create a customer fails
     */
    public void createCustomer(final LoginResponse loginResponse) {
        Log.d(TAG,"Inside createCustomer with loginResponse: " + loginResponse);
        APIClient.createCustomer(loginResponse);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    Fragment fragment = getFragmentManager().findFragmentByTag("tag_login");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface GettingLoginResponse {
        void lResponse(LoginResponse loginResponse);
    }

}
//**************************************************************************************************
//***********************************   From Login      ********************************************
//**************************************************************************************************

//                        if ( null != loginResponse.getToken() ) {
//                            Toast.makeText(view.getContext(), loginResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
//                        } else {
//                            Log.d(TAG, "Login Successful!");
////                            createCustomer(loginResponse);
//                        }

//                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(loginRequest));
//                        System.out.println(requestBody.toString());
//                        Request request = new Request.Builder()
//                                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/login")
//                                .post(requestBody)
//                                .build();
//
//                        client.newCall(request).enqueue(new Callback() {
//                            @Override
//                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                                getActivity().runOnUiThread(new Runnable() {
//                                    public void run() {
//                                        Toast.makeText(view.getContext(), "Error with request", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                                if (!response.isSuccessful()) {
//                                    throw new IOException("Unexpected code " + response);
//                                } else {
//                                    String resp = response.body().string();
//                                    Log.d("Response:", resp);
//                                    LoginResponse loginResponse = gson.fromJson(resp, LoginResponse.class);
//                                    createCustomer(loginResponse);
//                                }
//                            }
//                        });



//**************************************************************************************************
//***********************************   From createCustomer     ************************************
//**************************************************************************************************

//        try {
//            String[] tokenParts = loginResponse.getToken().split("\\.");
//             byte[] decodeBytes = Base64.decode(tokenParts[1], Base64.URL_SAFE);
//            JSONObject body = new JSONObject(new String(decodeBytes, "UTF-8"));
//            final FindUserProfileResponse profileResponse = gson.fromJson(body.get("user").toString(), FindUserProfileResponse.class);
//
//            System.out.println("The loginresponse token: " + loginResponse.getToken());
////            final CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(Integer.toString(profileResponse.getUserID()), profileResponse.getFirstName(), profileResponse.getFirstName(), profileResponse.getEmail());
//            final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), loginResponse.getToken());
////            Log.d(TAG, "Login id " + loginResponse.getUserId());
//
//            final String url = "https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/createclient";
//
//            Interceptor interceptor = new Interceptor() {
//                @NotNull
//                @Override
//                public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
//                    Request request = chain.request();
//                    Request newRequest = request.newBuilder()
//                            .url(url)
//                            .addHeader("Authorization","Bearer " + loginResponse.getToken())
//                            .post(requestBody)
//                            .build();
//
//                    return chain.proceed(newRequest);
//                }
//            };
//
//
//
//
//            Request request = new Request.Builder()
//                    .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/createclient")
//                    .addHeader("Authorization", "Bearer " + loginResponse.getToken())
//                    .post(requestBody)
//                    .build();
////            Response response = new
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(interceptor)
//                    .build();
//
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    Log.d(TAG, "Create client failed");
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
////                    if (!response.isSuccessful()) {
////                        throw new IOException("Unexpected code " + response.body().string());
////                    } else {
//                        String resp = response.body().string();
//                        Log.d("Response in creatClient ", resp);
//
////                        CreateCustomerResponse customerResponse = gson.fromJson(resp, CreateCustomerResponse.class);
//
////                        System.out.println(customerResponse.toString());
////                        if(!customerResponse.getSuccess()){
////                            Log.d("Response", "Customer Already Exists");
////                        }
//
////
////                        Intent myIntent = new Intent(getActivity(), Purchase.class);
////                        myIntent.putExtra("customerId", String.valueOf(loginResponse.getUserId()));
////                        myIntent.putExtra("authToken", loginResponse.getToken());//Optional parameters
////                        myIntent.putExtra("profile", profileResponse);
////                        getActivity().startActivity(myIntent);
//
//                    }
////                }
//            });
//        } catch (Exception e) {
//            //TODO login failed pop notification
//            e.printStackTrace();
//        }
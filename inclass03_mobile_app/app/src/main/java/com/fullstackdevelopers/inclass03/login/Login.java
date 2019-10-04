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
import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.dto.CreateCustomerRequest;
import com.fullstackdevelopers.inclass03.dto.CreateCustomerResponse;
import com.fullstackdevelopers.inclass03.dto.FindUserProfileResponse;
import com.fullstackdevelopers.inclass03.dto.LoginRequest;
import com.fullstackdevelopers.inclass03.dto.LoginResponse;
import com.fullstackdevelopers.inclass03.dto.SignupRequest;
import com.fullstackdevelopers.inclass03.services.GetHttp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class Login extends Fragment {
    private EditText et_email,password;
    private OkHttpClient client;
    private OnFragmentInteractionListener mListener;
    private View view;
    private Gson gson;
    private final String TAG = "Login";
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
        this.client = new OkHttpClient();
        this.gson = new Gson();

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

                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(loginRequest));
                        System.out.println(requestBody.toString());
                        Request request = new Request.Builder()
                                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/login")
                                .post(requestBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(view.getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                } else {
                                    String resp = response.body().string();
                                    Log.d("Response:", resp);
                                    LoginResponse loginResponse = gson.fromJson(resp, LoginResponse.class);
                                    createCustomer(loginResponse);
                                }
                            }
                        });
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
     * This method creates a new customer if the first call to create a customer fails
     */
    public void createCustomer(final LoginResponse loginResponse) {
        try {
            String[] tokenParts = loginResponse.getToken().split("\\.");
             byte[] decodeBytes = Base64.decode(tokenParts[1], Base64.URL_SAFE);
            JSONObject body = new JSONObject(new String(decodeBytes, "UTF-8"));
            FindUserProfileResponse profileResponse = gson.fromJson(body.get("user").toString(), FindUserProfileResponse.class);

            System.out.println(profileResponse.toString());
            final CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(Integer.toString(profileResponse.getUserID()), profileResponse.getFirstName(), profileResponse.getFirstName(), profileResponse.getEmail());
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(createCustomerRequest));
            System.out.println(requestBody.toString());
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + loginResponse.getToken())
                    .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/createclient")
                    .post(requestBody)
                    .build();

//        GetHttp createUser = new GetHttp(request);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "Create client failed");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        String resp = response.body().string();
                        Log.d("Response", resp);

                        CreateCustomerResponse customerResponse = gson.fromJson(resp, CreateCustomerResponse.class);

                        System.out.println(customerResponse.toString());
                        if(!customerResponse.getSuccess()){
                            Log.d("Response", "Customer Already Exists");
                        }

                        Intent myIntent = new Intent(getActivity(), HomeActivity.class);
                        myIntent.putExtra("customerId", createCustomerRequest.getId());
                        myIntent.putExtra("authToken", loginResponse.getToken());//Optional parameters
                        getActivity().startActivity(myIntent);

                    }
                }
            });
        } catch (Exception e) {
            //TODO login failed pop notification
            e.printStackTrace();
        }
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

}

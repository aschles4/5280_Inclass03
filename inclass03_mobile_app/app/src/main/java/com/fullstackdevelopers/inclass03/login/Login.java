package com.fullstackdevelopers.inclass03.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.R;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class Login extends Fragment {
    private EditText et_email,password;
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
        final OkHttpClient client = new OkHttpClient();
        final Gson gson = new Gson();

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
                                    String resp =  response.body().string();
                                    Log.d("Resposne:", resp);
                                    LoginResponse loginResponse = gson.fromJson(resp, LoginResponse.class);
//                                    Intent myIntent = new Intent(getActivity(), HomeActivity.class);
//                                    myIntent.putExtra("token", loginResponse.getToken()); //Optional parameters
//                                    getActivity().startActivity(myIntent);
                                    createCustomer(loginResponse);


                                }
                            }
                        });


//                        client.newCall(request).enqueue(new Callback() {
//                            @Override
//                            public void onFailure(Request request, IOException e) {
//                                Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onResponse(Response response) throws IOException {
//                                if (!response.isSuccessful()) {
//                                    throw new IOException("Unexpected code " + response);
//                                } else {
//                                    String resp =  response.body().string();
//                                    Log.d("Resposne:", resp);
//                                    LoginResponse loginResponse = gson.fromJson(resp, LoginResponse.class);
//                                    Intent myIntent = new Intent(getActivity(), HomeActivity.class);
//                                    myIntent.putExtra("token", loginResponse.getToken()); //Optional parameters
//                                    getActivity().startActivity(myIntent);
//                                }
//                            }
//                        });
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

//        view.findViewById(R.id.btn_temp).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LoginResponse loginResponse = new LoginResponse();
//                createCustomer(loginResponse);
////                Purchase cart = new Purchase();
////                Intent i = new Intent(getActivity(),cart.getClass());
////                startActivity(i);
//            }
//        });
    }

    /**
     * This method creates a new customer if the first call to create a customer fails
     */
    public void createCustomer(final LoginResponse loginResponse) {


        final Gson gson = new Gson();
        RequestBody requestBody;
        JsonObject objCust = new JsonObject();
        objCust.addProperty("id", loginResponse.getUserId());
//        objCust.addProperty("firstName","A");
//        objCust.addProperty("lastName", "M");
//        objCust.addProperty("email","am@blahsae.com");

        final String custObj = gson.toJson(objCust);

        Log.d(TAG, "The result after gson: " + custObj);

        requestBody = RequestBody.create(JSON, custObj);
        Request request = new Request.Builder()
                .url(" https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/create_client")
                .post(requestBody)
                .build();

        GetHttp createUser = new GetHttp(request);
        createUser.setGetRespListener(new GetHttp.GetRespListener() {
            @Override
            public void r(Response res) {
                if ( res != null ) {
//                    try {
                        Log.d(TAG, "The result createCustomer is: " + res.toString());
//                        JSONObject jsonObject = new JSONObject(res.body().string());
//                        JSONObject jsonObject1 = jsonObject.getJSONObject("token");
//
//                        JSONObject jsonObject2 = jsonObject1.getJSONObject("customer");
//                        Log.d(TAG, "The result createCustomer is: " + jsonObject2.toString());
//                        String customer = gson.toJson(jsonObject2);

                        Intent myIntent = new Intent(getActivity(), HomeActivity.class);
                        try {
                            myIntent.putExtra("token", /*loginResponse.getToken()*/ custObj); //Optional parameters
                            getActivity().startActivity(myIntent);
                        }catch(Exception e){
//                                        Toast.makeText(view.getContext(), "Unable to Login", Toast.LENGTH_SHORT).show();
                        }
//                    } catch ( IOException  e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });
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

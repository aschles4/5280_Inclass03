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
import com.fullstackdevelopers.inclass03.dto.LoginRequest;
import com.fullstackdevelopers.inclass03.dto.LoginResponse;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class Login extends Fragment {
    private EditText et_email,password;
    private OnFragmentInteractionListener mListener;
    private View view;
    private Gson gson;

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
                        Request request = new Request.Builder()
                                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/login")
                                .post(requestBody)
                                .build();


                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                } else {
                                    String resp =  response.body().string();
                                    Log.d("Resposne:", resp);
                                    LoginResponse loginResponse = gson.fromJson(resp, LoginResponse.class);
                                    Intent myIntent = new Intent(getActivity(), HomeActivity.class);
                                    myIntent.putExtra("token", loginResponse.getToken()); //Optional parameters
                                    getActivity().startActivity(myIntent);
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

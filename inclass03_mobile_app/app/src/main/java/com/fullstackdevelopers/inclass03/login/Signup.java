package com.fullstackdevelopers.inclass03.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.dto.LoginRequest;
import com.fullstackdevelopers.inclass03.dto.LoginResponse;
import com.fullstackdevelopers.inclass03.dto.SignupRequest;
import com.fullstackdevelopers.inclass03.profile.ProfileView;
import com.fullstackdevelopers.inclass03.services.GetHttp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.sql.Time;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class Signup extends Fragment {
    private Context myContext;
    private Bitmap pic;
    View view;
    private OnFragmentInteractionListener mListener;
    private final String TAG = "Signup";

    public Signup() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final OkHttpClient client = new OkHttpClient();
        final Gson gson = new Gson();

        view.findViewById(R.id.b_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText fname, lname, email, password, rpassword, age, weight, address;
                fname = view.findViewById(R.id.et_firstname);
                lname = view.findViewById(R.id.et_lastname);
                email = view.findViewById(R.id.et_email);
                password = view.findViewById(R.id.et_password);
                rpassword = view.findViewById(R.id.et_rpassword);
                age = view.findViewById(R.id.age);
                weight = view.findViewById(R.id.weight);
                address = view.findViewById(R.id.address);

                final String f = fname.getText().toString();
                final String l = lname.getText().toString();
                final String e = email.getText().toString();
                final String p = password.getText().toString();
                final String rp = rpassword.getText().toString();
                final String ageVal = age.getText().toString();
                final String weightVal = weight.getText().toString();
                final String addressVal = address.getText().toString();

                if (!f.isEmpty() && !l.isEmpty() && !e.isEmpty() && !p.isEmpty() && !rp.isEmpty()) {
                    if (isValidEmail(e)) {
                        if (rp.equals(p)) {
                           //TODO insert signup call here
                            final SignupRequest signupRequest = new SignupRequest();
                            signupRequest.setEmail(e);
                            signupRequest.setFirstname(f);
                            signupRequest.setLastname(l);
                            signupRequest.setPassword(p);
                            signupRequest.setAge(Integer.parseInt(ageVal));
                            signupRequest.setWeight(Double.parseDouble(weightVal));
                            signupRequest.setAddress(addressVal);

                            System.out.println(signupRequest.toString());

                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(signupRequest));
                            Request request = new Request.Builder()
                                    .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/signup")
                                    .post(requestBody)
                                    .build();
                            Log.d(TAG, "reauest: " +request);

                            Log.d(TAG, "request header: " +request.headers());

                            Log.d(TAG, "Request body: " + gson.toJson(signupRequest));


                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        throw new IOException("Unexpected code " + response);
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                Log.d(TAG, "This is the response from signup: " + response.body().toString());
                                                Toast.makeText(getContext(), "User Signed Up Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }

                            });
                        } else {
                            Toast.makeText(getContext(), "Password  mismatch", Toast.LENGTH_SHORT).show();
                            password.setText("");
                            rpassword.setText("");
                        }
                    } else {
                        Toast.makeText(getContext(), "Invalid Email!", Toast.LENGTH_SHORT).show();
                        email.setText("");
                    }
                } else {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }



            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLogin();

            }
        });
        view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLogin();
            }
        });
    }

    @Override
    public void onAttach(Context context) {

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        myContext = context;
        super.onAttach(context);

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

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void returnToLogin() {
        Log.d("signup", "Return to login before frag l");
        Fragment l = getFragmentManager().findFragmentByTag("tag_login");
        Log.d("signup", "Return to login after frag l" + l);
        getFragmentManager().beginTransaction()
                .replace(R.id.main_layout, l)
                .addToBackStack("tag_signup")
                .commit();
    }
}


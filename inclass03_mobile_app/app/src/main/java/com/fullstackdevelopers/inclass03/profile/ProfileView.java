package com.fullstackdevelopers.inclass03.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.dto.EditUserProfileRequest;
import com.fullstackdevelopers.inclass03.dto.FindUserProfileRequest;
import com.fullstackdevelopers.inclass03.dto.FindUserProfileResponse;
import com.fullstackdevelopers.inclass03.dto.LogoutRequest;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;


public class ProfileView extends Fragment {
    OnFragmentInteractionListener mListener;
    String customerId;
    private String authToken;
    View view;
    OkHttpClient client;
    Gson gson;

    public ProfileView(String customerId, String authToken) {
        this.customerId = customerId;
        this.authToken = authToken;
        client = new OkHttpClient();
        gson = new Gson();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_view, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final EditText fName = view.findViewById(R.id.fn);
        final EditText lName = view.findViewById(R.id.ln);
        final EditText email = view.findViewById(R.id.em);
        final EditText pass = view.findViewById(R.id.pw);
        final EditText age = view.findViewById(R.id.ag);
        final EditText weight = view.findViewById(R.id.we);
        final EditText address = view.findViewById(R.id.ad);
        //TODO Call user profile endpoint

        final FindUserProfileRequest findUserProfileRequest = new FindUserProfileRequest();
        findUserProfileRequest.setToken(customerId);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(findUserProfileRequest));
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + authToken)
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/findProfile")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("call", e.getMessage());
                Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    final FindUserProfileResponse findUserProfileResponse = gson.fromJson(response.body().string(), FindUserProfileResponse.class);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            Log.d("Fucked", findUserProfileResponse.toString());
                            fName.setText(findUserProfileResponse.getFirstName());
                            lName.setText(findUserProfileResponse.getLastName());
                            pass.setText(findUserProfileResponse.getPassword());
                            email.setText(findUserProfileResponse.getEmail());
                            age.setText(Integer.toString(findUserProfileResponse.getAge()));
                            weight.setText(Float.toString(findUserProfileResponse.getWeight()));
                            address.setText(findUserProfileResponse.getAddress());
                        }
                    });

                }
            }
        });

        view.findViewById(R.id.btn_profile_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("profile", "Edit user profile");

                final EditUserProfileRequest editUserProfileRequest = new EditUserProfileRequest();
                editUserProfileRequest.setToken(customerId);
                editUserProfileRequest.setFirstname(fName.getText().toString());
                editUserProfileRequest.setLastname(lName.getText().toString());
                editUserProfileRequest.setEmail(email.getText().toString());
                editUserProfileRequest.setPassword(pass.getText().toString());
                editUserProfileRequest.setAge(age.getText().toString());
                editUserProfileRequest.setWeight(weight.getText().toString());
                editUserProfileRequest.setAddress(address.getText().toString());

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(editUserProfileRequest));
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + authToken)
                        .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/editProfile")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), "User Info updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });


//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            throw new IOException("Unexpected code " + response);
//                        } else {
//                            getActivity().runOnUiThread(new Runnable() {
//                                public void run() {
//                                    Toast.makeText(getContext(), "User Info updated successfully", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    }
//                });

            }
        });
        view.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LogoutRequest logoutRequest = new LogoutRequest();
                logoutRequest.setToken(authToken);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(logoutRequest));
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + authToken)
                        .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/logout")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                            returnToLogin();
                        }
                    }
                });


//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        Toast.makeText(getContext(), "Error with request", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            throw new IOException("Unexpected code " + response);
//                        } else {
//                            returnToLogin();
//                        }
//                    }
//                });
            }
        });

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

    public void returnToLogin() {
//        Log.d("signup", "Return to login before frag l");
        Fragment l = getFragmentManager().findFragmentByTag("tag_login");
        Log.d("signup", "Return to login after frag l" + l);
        getFragmentManager().beginTransaction()
                .replace(R.id.main_layout, l)
                .addToBackStack("tag_signup")
                .commit();
    }

}

package com.fullstackdevelopers.inclass03;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import com.fullstackdevelopers.inclass03.login.Login;
import com.fullstackdevelopers.inclass03.login.Signup;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements Login.OnFragmentInteractionListener,
        Signup.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_layout, new Login(), "tag_login")
                .commit();
    }



    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
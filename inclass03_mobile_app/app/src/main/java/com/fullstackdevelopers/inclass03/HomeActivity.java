package com.fullstackdevelopers.inclass03;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.fullstackdevelopers.inclass03.login.Login;
import com.fullstackdevelopers.inclass03.profile.ProfileView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity implements Login.OnFragmentInteractionListener, ProfileView.OnFragmentInteractionListener {


    public HomeActivity() { }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        String token = (String) intent.getSerializableExtra("token");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.home_layout, new ProfileView(token), "tag_home")
                .commit();
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}

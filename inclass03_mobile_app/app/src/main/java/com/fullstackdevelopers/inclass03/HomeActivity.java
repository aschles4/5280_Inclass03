package com.fullstackdevelopers.inclass03;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.fullstackdevelopers.inclass03.cart.CartView;
import com.fullstackdevelopers.inclass03.dto.FindUserProfileResponse;
import com.fullstackdevelopers.inclass03.login.Login;
import com.fullstackdevelopers.inclass03.products.ProductView;
import com.fullstackdevelopers.inclass03.products.ProductsView;
import com.fullstackdevelopers.inclass03.profile.ProfileView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity implements Login.OnFragmentInteractionListener, ProfileView.OnFragmentInteractionListener,
        ProductsView.OnFragmentInteractionListener, CartView.OnFragmentInteractionListener, ProductView.OnFragmentInteractionListener {


    public HomeActivity() { }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ProductsView p = new ProductsView();
        Intent intent = getIntent();
        String customerId = intent.getStringExtra("customerId");
        String authToken = intent.getStringExtra("authToken");
        FindUserProfileResponse userProfileResponse = (FindUserProfileResponse) intent.getSerializableExtra("profile");
        Bundle b = new Bundle();
        b.putString("customerId", customerId);
        b.putString("authToken", authToken);
        b.putSerializable("profile", userProfileResponse);
        p.setArguments(b);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.home_layout, p, "tag_products_view")
                .commit();
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}

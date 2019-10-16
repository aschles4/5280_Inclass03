package com.fullstackdevelopers.inclass03;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.fullstackdevelopers.inclass03.cart.CartView;
import com.fullstackdevelopers.inclass03.login.Login;
import com.fullstackdevelopers.inclass03.products.ProductView;
import com.fullstackdevelopers.inclass03.products.ProductsView;
import com.fullstackdevelopers.inclass03.profile.ProfileView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static com.estimote.coresdk.common.config.EstimoteSDK.getApplicationContext;

public class HomeActivity extends AppCompatActivity implements Login.OnFragmentInteractionListener, ProfileView.OnFragmentInteractionListener,
        ProductsView.OnFragmentInteractionListener, CartView.OnFragmentInteractionListener, ProductView.OnFragmentInteractionListener {


    public HomeActivity() { }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        Intent intent = getIntent();
        String token = (String) intent.getSerializableExtra("token");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.home_layout, new ProductsView(token), "tag_products_view")
                .commit();
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}

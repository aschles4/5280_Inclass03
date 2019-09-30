package com.fullstackdevelopers.inclass03.products;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.cart.CartView;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.data.Product;
import com.fullstackdevelopers.inclass03.profile.ProfileView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsView extends Fragment implements ProductsAdapter.OnProductListener {
    private OnFragmentInteractionListener mListener;
    private View view;
    ArrayList<Product> products = new ArrayList<>();
    private String token;

    public ProductsView() {
    }

    //Not sure what to do with Token, but got it
    public ProductsView(String token) {
        this.token = token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bottomNav();
        productList();
        Cart cart = getCart(getContext());
        Log.d("cart", cart.toString());
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

    @Override
    public void OnProductClick(int position) {
        Cart cart = getCart(view.getContext());
        cartValue(cart);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void bottomNav() {

        view.findViewById(R.id.nav_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartView p = new CartView(token);
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, p, "tag_cart_view")
                        .addToBackStack("tag_products_view")
                        .commit();
            }
        });

        view.findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ProfileView p = new ProfileView(token);
            getFragmentManager().beginTransaction()
                    .replace(R.id.home_layout, p, "tag_profile_view")
                    .addToBackStack("tag_products_view")
                    .commit();

            }
        });
        view.findViewById(R.id.nav_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment l = getFragmentManager().findFragmentByTag("tag_login");
                Log.d("signup", "Return to login after frag l" + l);
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, l)
                        .addToBackStack("tag_signup")
                        .commit();
            }
        });
    }

    //Initialze the RecyclerView and Currently uses a test data set
    public void productList() {
        RecyclerView recyclerView;
        RecyclerView.Adapter mAdapter;
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView = view.findViewById(R.id.productsList);
        recyclerView.setLayoutManager(layoutManager);
        //Start of Test Data
        for (int i = 0; i < 5; i++) {
            Product knees = new Product();
            knees.setId("" + i);
            knees.setName("Product " + i);
            knees.setPrice(42.69);
            products.add(knees);
        }
        //End of Test Data
        mAdapter = new ProductsAdapter(products, ProductsView.this);
        recyclerView.setAdapter(mAdapter);
    }

    //If no Cart exists create one
    public void initCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    //getCart from localstorage
    public Cart cartFromLocalStorage(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cart cart = (Cart) ois.readObject();
        cartValue(cart);
        return cart;
    }

    //moved code here to look cleaner because of try/catch blocks
    public Cart getCart(Context context) {
        Cart cart = null;
        try {
            cart = cartFromLocalStorage(context, "CART");
        } catch (Exception e) {
            ArrayList<Product> products = new ArrayList<>();
            try {
                initCart(context, "CART", new Cart(UUID.randomUUID().toString(), "UserID", products, 0));
                cart = cartFromLocalStorage(context, "CART");
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return cart;
    }

    //update the badge over the cart based on cart.size()
    public void cartValue(Cart cart) {
        ImageView badge = view.findViewById(R.id.cartSize);
        TextView cartValue = view.findViewById(R.id.cartValue);
        ArrayList<Product> products = cart.getProducts();
        int totalProducts = products.size();
        if (totalProducts != 0) {
            badge.setVisibility(View.VISIBLE);
            cartValue.setText(String.valueOf(totalProducts));
        }
    }


}
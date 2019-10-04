package com.fullstackdevelopers.inclass03.cart;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.products.ProductsView;
import com.fullstackdevelopers.inclass03.R;
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

public class CartView extends Fragment implements CartAdapter.OnProductListener {
    private OnFragmentInteractionListener mListener;
    private View view;
    private String customerId;
    private String authToken;

    public CartView() {
    }

    public CartView(String customerId, String authToken) {
        this.customerId = customerId;
        this.authToken = authToken;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cart, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bottomNav();
        Cart cart = getCart(getContext());
        Log.d("cart", cart.toString());
        cartAdapter(cart);
        purchase();
        cartValue(cart);

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
        try {
            Cart cart = retriveCart(view.getContext(), "CART");
            cartValue(cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void cartAdapter(Cart cart) {
        RecyclerView recyclerView;
        RecyclerView.Adapter mAdapter;
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView = view.findViewById(R.id.cartList);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<Product> products = cart.getProducts();
        mAdapter = new CartAdapter(products, (CartAdapter.OnProductListener) CartView.this);
        recyclerView.setAdapter(mAdapter);
    }

    public void bottomNav() {
        view.findViewById(R.id.nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductsView c = new ProductsView();
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, c, "tag_products_view")
                        .addToBackStack("tag_products_view")
                        .commit();
            }
        });

        view.findViewById(R.id.nav_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartView p = new CartView();
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, p, "tag_cart_view")
                        .addToBackStack("tag_products_view")
                        .commit();
            }
        });

        view.findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileView p = new ProfileView(customerId, authToken);
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, p, "tag_profile_view")
                        .addToBackStack("tag_products_view")
                        .commit();

            }
        });
        view.findViewById(R.id.nav_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            HomeActivity home = new HomeActivity();
            Intent i = new Intent(getContext(),home.getClass());
            i.putExtra("customerId",customerId);
            i.putExtra("authToken",authToken);
            startActivity(i);
            }
        });
    }

    public void initCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public Cart retriveCart(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cart cart = (Cart) ois.readObject();
        return cart;
    }

    public Cart getCart(Context context) {
        Cart cart = null;
        try {
            cart = retriveCart(context, "CART");
        } catch (Exception e) {
            ArrayList<Product> products = new ArrayList<>();
            try {
                initCart(context, "CART", new Cart(UUID.randomUUID().toString(), "2", products, 0));
                cart = retriveCart(context, "CART");

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return cart;
    }


    public void purchase() {
        Button purchase;
        purchase = view.findViewById(R.id.purchaseButton);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Cart cart = retriveCart(view.getContext(), "CART");
                    Toast toast = Toast.makeText(view.getContext(), "PURCHASE COST: " + (cart.getTotalPrice()), Toast.LENGTH_LONG);
                    toast.show();
                    Log.d("PURCHASE", String.valueOf(cart.getTotalPrice()));
                    Cart empty = new Cart(cart.getUid(),"UID",new ArrayList<Product>(),0);
                    cartValue(empty);
                    updateCart(getContext(),"CART",empty);
                    Purchase items = new Purchase();
                    Intent i = new Intent(getActivity(),items.getClass());
                    i.putExtra("customerId", customerId);
                    i.putExtra("price",String.valueOf(cart.getTotalPrice()));
                    startActivity(i);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void cartValue(Cart cart) {
        ImageView badge = view.findViewById(R.id.cartSize);
        TextView cartValue = view.findViewById(R.id.cartValue);
        ArrayList<Product> products = cart.getProducts();
        int totalProducts = products.size();
        if (totalProducts != 0) {
            badge.setVisibility(View.VISIBLE);
            cartValue.setText(String.valueOf(totalProducts));
        } else {
            badge.setVisibility(View.INVISIBLE);
            cartValue.setVisibility(View.INVISIBLE);
        }
    }
    public void updateCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }
}


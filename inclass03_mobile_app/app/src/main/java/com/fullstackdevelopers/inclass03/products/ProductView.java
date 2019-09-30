package com.fullstackdevelopers.inclass03.products;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.data.Product;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


//This does not need to be implemented for the assignment
public class ProductView extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Product product;
    private View view;
    public ProductView(Product product) {
        this.product=product;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_product_view, container, false);
        return view;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView productName,productPrice,productDesc;
        ImageView addCart, productImage;
        productName = view.findViewById(R.id.productViewName);
        productPrice = view.findViewById(R.id.productViewPrice);
        productDesc = view.findViewById(R.id.productViewDesc);
        productName.setText(product.getName());
        productPrice.setText(String.valueOf(product.getPrice()));

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

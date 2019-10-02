package com.fullstackdevelopers.inclass03.cart;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.data.Product;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {
    private ArrayList<Product> mDataset;
    private View v;
    private Cart cart;
    private OnProductListener onProductListener;


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView productName, productPrice;
        public ImageView removeCart, cartImage;

        public MyViewHolder(View v) {
            super(v);
            productName = v.findViewById(R.id.name);
            productPrice = v.findViewById(R.id.price);
            cartImage = v.findViewById(R.id.cartImage);
            removeCart = v.findViewById(R.id.removeCart);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public CartAdapter(ArrayList<Product> myDataset, OnProductListener onProductListener) {
        mDataset = myDataset;
        this.onProductListener = onProductListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_info, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        try {
            Product product = mDataset.get(position);
            holder.productName.setText(product.getName());
            String price = String.valueOf(product.getPrice());
            holder.productPrice.setText(price);
            try {
                Picasso.get().load(product.getUrl()).into(holder.cartImage);
            } catch (Exception e) {
                holder.cartImage.setImageResource(R.drawable.ic_account_circle_24px);
            }
            try {
                cart = retriveCart(v.getContext(), "CART");
            } catch (Exception e) {
                e.printStackTrace();
            }
            removeCart(holder.removeCart, product, position);
        } catch (Exception e) {
            Log.d("CA", e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnProductListener {
        void OnProductClick(int position);
    }

    public Cart retriveCart(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cart cart = (Cart) ois.readObject();
        return cart;
    }

    public void removeCart(ImageView removeCart, final Product product, final int position) {
        removeCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Log.d("CA", "View product Product: " + product.getId());
                  try {
                      ArrayList<Product> products = cart.getProducts();
                      Double totalPrice = cart.getTotalPrice();
                      totalPrice -= product.getPrice();
                      cart.setProducts(products);
                      cart.setTotalPrice(totalPrice);

                      try {
                          mDataset.remove(position);
                          products.remove(position);

                          notifyItemRemoved(position);
                          updateCart(v.getContext(), "CART", cart);
                          onProductListener.OnProductClick(0);


                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  }catch(Exception e){
                      e.printStackTrace();
                  }

            }
        });
    }

    public void updateCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }
}
package com.fullstackdevelopers.inclass03.products;

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

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyViewHolder> {
    private ArrayList<Product> mDataset;
    private ProductsAdapter.OnProductListener onProductListener;
    private View v;
    private Cart cart;



    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView productName,productPrice;
        public ImageView addCart,productImage;
        ProductsAdapter.OnProductListener onProductListener;
        public MyViewHolder(View v, ProductsAdapter.OnProductListener onProductListener) {
            super(v);
            productName= v.findViewById(R.id.name);
            productPrice = v.findViewById(R.id.price);
            productImage = v.findViewById(R.id.productImage);
            addCart=v.findViewById(R.id.addCart);
            this.onProductListener = onProductListener;
            addCart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onProductListener.OnProductClick(getAdapterPosition());

        }
    }
    public ProductsAdapter(ArrayList<Product> myDataset, ProductsAdapter.OnProductListener onProductListener) {
        mDataset = myDataset;
        this.onProductListener = onProductListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_info, parent, false);
        MyViewHolder vh = new MyViewHolder(v, onProductListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        try {
            cart = retriveCart(v.getContext(),"CART");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Product product=mDataset.get(position);
        holder.productName.setText(product.getName());
        String price = String.valueOf(product.getPrice());
        holder.productPrice.setText(price);
        try {
            Picasso.get().load(product.getUrl()).into(holder.productImage);
//            holder.productImage.setImageResource(R.drawable.ic_account_circle_24px);
        }catch (Exception e){
            holder.productImage.setImageResource(R.drawable.ic_account_circle_24px);
        }
        addCart(holder.addCart,product);

    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnProductListener{
        void OnProductClick(int position);
    }
    public Cart retriveCart(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cart cart = (Cart) ois.readObject();
        return cart;
    }

    public void addCart(ImageView addCart, final Product product){
        addCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PA","adding to Cart! Product: "+product.getId());
                ArrayList<Product> products = cart.getProducts();
                Double totalPrice = cart.getTotalPrice();
                totalPrice+=product.getPrice();
                products.add(product);
                cart.setProducts(products);
                cart.setTotalPrice(totalPrice);
                try {
                    updateCart(v.getContext(),"CART",cart);
                    onProductListener.OnProductClick(1);

                } catch (IOException e) {
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
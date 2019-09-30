package com.fullstackdevelopers.inclass03.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {
    private String id;
    private String uid;
    private ArrayList<Product> products = new ArrayList<>();
    private double totalPrice;

    public Cart() {
    }

    public Cart(String id, String uid, ArrayList<Product> products, double totalPrice) {
        this.id = id;
        this.uid = uid;
        this.products = products;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", products=" + products +
                ", totalPrice=" + totalPrice +
                '}';
    }
}

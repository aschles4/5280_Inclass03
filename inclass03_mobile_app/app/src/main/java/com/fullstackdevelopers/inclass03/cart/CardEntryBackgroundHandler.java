package com.fullstackdevelopers.inclass03.cart;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.services.GetHttp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sqip.CardDetails;
import sqip.CardEntryActivityCommand;
import sqip.CardNonceBackgroundHandler;

public class CardEntryBackgroundHandler extends AppCompatActivity implements CardNonceBackgroundHandler {
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private int price;
    private Context context;
    private String token;
    CardEntryBackgroundHandler(int price ,Context context,String token){
        this.context=context;
        this.price=price;
        this.token = token;
    }
    @Override
    public CardEntryActivityCommand handleEnteredCardInBackground(CardDetails cardDetails) {
        Log.d("cardDetails",cardDetails.getNonce());
        final JsonObject newPayment = new JsonObject();
        newPayment.addProperty("amount", price);
        newPayment.addProperty("nonce", cardDetails.getNonce());
        Gson gson = new Gson();
        String d = gson.toJson(newPayment);
        final RequestBody requestBody = RequestBody.create(JSON, d);
        final Request request = new Request.Builder()
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/payment")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("response",call.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Transaction Failed", Toast.LENGTH_SHORT).show();
                       // goHome();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d("response",response.message());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Transaction successful ", Toast.LENGTH_SHORT).show();
                     //   goHome();
                    }
                });

            }
        });

        return new CardEntryActivityCommand.Finish();
    }

//    public void goHome() {
////        Toast.makeText(getApplicationContext(),"Thanks for your purchase!",Toast.LENGTH_LONG);
//        HomeActivity home = new HomeActivity();
//        Intent i = new Intent(context,home.getClass());
//        i.putExtra("token",token);
//        startActivity(i);
//    }
}


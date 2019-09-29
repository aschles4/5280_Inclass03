package com.fullstackdevelopers.inclass03.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Json;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.BraintreePaymentResultListener;
import com.braintreepayments.api.models.BraintreePaymentResult;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.fullstackdevelopers.inclass03.dto.Token;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minidev.json.parser.JSONParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class Cart extends AppCompatActivity implements BraintreePaymentResultListener, BraintreeErrorListener,
        BraintreeCancelListener, BraintreeListener {

    private static final int REQUEST_CODE = 200;
    private static final int RESULT_OK = -1;
    private static final String JSON = "Json";
    private BraintreeFragment mBraintreeFragment;
    private String mAuthorization;
    private static final String TAG = "Cart";
    private String key;
    private View v;
    private String clientToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Gson gson = new Gson();

        final OkHttpClient client = new OkHttpClient();
        v = findViewById(android.R.id.content);

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8383/client_token")
                .get()
                .build();

        Log.d(TAG, "In cart with request: " + request);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "Failure to communicate!");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {

                    key = response.body().string();
                    JSONObject jsonObject = new JSONObject(key);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("token");
                    clientToken = jsonObject1.getString("clientToken");
                    Log.d(TAG, "The key is: " + jsonObject1.getString("clientToken"));
                    onBraintreeSubmit(jsonObject1.getString("clientToken"), v);

                } catch ( Exception e ) {
                    e.printStackTrace();
                }


            }
        });

        DropInResult.fetchDropInResult(Cart.this, clientToken, new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                // an error occurred
                Log.d(TAG, "Something went wrong!");
                exception.printStackTrace();
            }

            @Override
            public void onResult(DropInResult result) {
                if (result.getPaymentMethodType() != null) {
                    // use the icon and name to show in your UI
                    int icon = result.getPaymentMethodType().getDrawable();
                    int name = result.getPaymentMethodType().getLocalizedName();

                    PaymentMethodType paymentMethodType = result.getPaymentMethodType();
                    if (paymentMethodType == PaymentMethodType.GOOGLE_PAYMENT) {
                        // The last payment method the user used was Google Pay.
                        // The Google Pay flow will need to be performed by the
                        // user again at the time of checkout.
                    } else {
                        // Use the payment method show in your UI and charge the user
                        // at the time of checkout.
                        PaymentMethodNonce paymentMethod = result.getPaymentMethodNonce();
                    }
                } else {
                    // there was no existing payment method
                    onBraintreeSubmit(clientToken, v);
                }
            }

        });



    }

    public void onBraintreeSubmit(String key, View v) {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(key);
        Log.d(TAG, "Made it inside onBraintreeSubmit");
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Inside onActivityResult" + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                Gson gson = new GsonBuilder().serializeNulls().create();

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String obj = gson.toJson(result);

                OkHttpClient client = new OkHttpClient();

                Log.d(TAG, "This is the payment info: " + result.getPaymentMethodNonce().getNonce() + " json object: " + obj);

                RequestBody requestBody = RequestBody.create(JSON, obj);
                final Request request = new Request.Builder()
                        .url("http://10.0.2.2:8383/sale")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.d(TAG, "This is the response " + response + " Call: " + call);
                    }
                });


                Log.d(TAG, "The key is: " + result.getPaymentMethodNonce());
                // use the result to update your UI and send the payment method nonce to your server
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    @Override
    public void onCancel(int requestCode) {
        // Use this to handle a canceled activity, if the given requestCode is important.
        // You may want to use this callback to hide loading indicators, and prepare your UI for input
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
            BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
            if (cardErrors != null) {
                // There is an issue with the credit card.
                BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                if (expirationMonthError != null) {
                    // There is an issue with the expiration month.
                    setErrorMessage(expirationMonthError.getMessage());
                }
            }
        }
    }

    private void setErrorMessage(String message) {

    }

    @Override
    public void onBraintreePaymentResult(BraintreePaymentResult result) {

    }


}



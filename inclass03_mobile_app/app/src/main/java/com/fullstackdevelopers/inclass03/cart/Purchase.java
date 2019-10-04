package com.fullstackdevelopers.inclass03.cart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.BraintreePaymentResultListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.BraintreePaymentResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.cardform.view.CardForm;
import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.data.Product;
import com.fullstackdevelopers.inclass03.dto.CreateClientTokenRequest;
import com.fullstackdevelopers.inclass03.dto.CreateCustomerRequest;
import com.fullstackdevelopers.inclass03.dto.CreateSaleRequest;
import com.fullstackdevelopers.inclass03.dto.Options;
import com.fullstackdevelopers.inclass03.dto.UpdateCustomerRequest;
import com.fullstackdevelopers.inclass03.services.GetHttp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Purchase extends AppCompatActivity implements PaymentMethodNonceCreatedListener, BraintreePaymentResultListener, BraintreeErrorListener,
        BraintreeCancelListener, BraintreeListener {

    private static final int REQUEST_CODE = 200;
    private static final int RESULT_OK = -1;
    private BraintreeFragment mBraintreeFragment;
    private String mAuthorization;
    private static final String TAG = "Purchase";
    private String key;
    private View v;
    private String clientToken;
    private String authToken;
    private boolean isCreated;
    private boolean isUpdated;
    private boolean exists;
    private MediaType JSON = MediaType.parse("application/json");
    private String customerId;
    private String price;
    private String nonce;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customerId = getIntent().getStringExtra("customerId");
        authToken = getIntent().getStringExtra("authToken");
        price = getIntent().getStringExtra("price");

        gson = new Gson();
        RequestBody requestBody;

        v = findViewById(android.R.id.content);
//        JsonObject objCust = new JsonObject();
//        objCust.addProperty("id","dFhtU9wM");
//
//        String someObj = gson.toJson(objCust);
        Log.d(TAG, "The objKey: " + customerId);

        CreateClientTokenRequest r = new CreateClientTokenRequest(customerId);

        requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(r));
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + authToken)
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/create_token")
                .post(requestBody)
                .build();
        // This is the first call to create a clientToken to proceed with payment
        // If call fails could mean customer doesn't exist and so it passes
        // to createCustomer(), if it succeeds, then it passes to onBraintreeSubmit()
        GetHttp data = new GetHttp(request);
        data.setGetRespListener(new GetHttp.GetRespListener() {
            @Override
            public void r(Response res) {
                if ( res != null ) {
                    try {
                        JSONObject jObj = new JSONObject(res.body().string());
                        Log.d(TAG, "Made it in first call to get token: " + jObj.toString());
//                        JSONObject jObj2 = jObj.getJSONObject("token");
//                        Log.d(TAG, "Made it in first call to get token: " + jObj2.get("success"));
                        if ( jObj.get("success").equals(false) ) {
                            Log.d(TAG, "Something went wrong, need to investigate!");
                        } else {
                            clientToken = jObj.getString("clientToken");
                            Log.d(TAG, "Customer exists going to UI with clientToken: " + clientToken);

                            onBraintreeSubmit(clientToken, v);
                        }
                    } catch ( JSONException | IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * This is the method that pulls up the dropin UI from braintree. If the
     * client has existing payment methods stored on braintree, they will be displayed
     * @param key
     * @param v
     */
    public void onBraintreeSubmit(String key, View v) {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(key)
                .vaultManager(true)
                .cardholderNameStatus(CardForm.FIELD_REQUIRED);
        Log.d(TAG, "Made it inside onBraintreeSubmit");
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }

    /**
     * This method is called on completion of the dropin UI with a payment method.
     * Inside it will attempt to save the payment method, but it won't on first time use after
     * creating the customer in braintree. Second will save. It will pass to makeSale() whether card saves or not
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Inside onActivityResult" + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                isCreated = false;

                OkHttpClient client = new OkHttpClient();
                System.out.println(gson.toJson(result.getPaymentMethodNonce()));
                Log.d(TAG, "This is the payment info: " + gson.toJson(result.getPaymentMethodNonce()) + " json object: "  + result.toString());

                UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(customerId, result.getPaymentMethodNonce());
                final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(updateCustomerRequest));
                final Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + authToken)
                        .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/update_client")
                        .post(requestBody)
                        .build();


                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Gson gson = new Gson();
                        Log.d(TAG, "The response on update: " + response.body().string());


                        Log.d(TAG, "Client Updated");
                        Double p = Double.parseDouble(price);

                        DecimalFormat df = new DecimalFormat("########.##");
                        System.out.println(df.format(p));
                        makeSale(result.getPaymentMethodNonce(), df.format(p));
                    }
                });

                // use the result to update your UI and send the payment method nonce to your server
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    /**
     * This method creates the transaction
     * @param
     */
    public void makeSale(PaymentMethodNonce paymentMethodNonce, String price) {
        OkHttpClient client = new OkHttpClient();

        Options options = new Options(true);

        CreateSaleRequest createSaleRequest = new CreateSaleRequest(paymentMethodNonce, price, options);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(createSaleRequest));
        final Request requestTrans = new Request.Builder()
                .addHeader("Authorization", "Bearer " + authToken)
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/sale")
                .post(requestBody)
                .build();

        client.newCall(requestTrans).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "This is the response in sale " + response.body().string());
                runOnUiThread(new Runnable() {
                    public void run() {
                        goHome();
                    }

                });

            }
        });

    }


    public void goHome() {
//        Toast.makeText(getApplicationContext(),"Thanks for your purchase!",Toast.LENGTH_LONG);
        Cart empty = new Cart("0","UID",new ArrayList<Product>(),0);
//        updateCart(empty);
        HomeActivity home = new HomeActivity();
        Intent i = new Intent(getApplicationContext(),home.getClass());
        i.putExtra("customerId",customerId);
        i.putExtra("authToken",authToken);
        startActivity(i);
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
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        // Send this nonce to your server
        this.nonce = paymentMethodNonce.getNonce();
    }

    @Override
    public void onBraintreePaymentResult(BraintreePaymentResult result) {

    }
    public void updateCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }



}



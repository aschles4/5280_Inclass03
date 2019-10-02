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
import com.braintreepayments.api.models.BraintreePaymentResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.cardform.view.CardForm;
import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.data.Product;
import com.fullstackdevelopers.inclass03.services.GetHttp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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
import java.util.ArrayList;

public class Purchase extends AppCompatActivity implements BraintreePaymentResultListener, BraintreeErrorListener,
        BraintreeCancelListener, BraintreeListener {

    private static final int REQUEST_CODE = 200;
    private static final int RESULT_OK = -1;
    private BraintreeFragment mBraintreeFragment;
    private String mAuthorization;
    private static final String TAG = "Purchase";
    private String key;
    private View v;
    private String clientToken;
    private boolean isCreated;
    private boolean isUpdated;
    private boolean exists;
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String token;
    private String price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        token = getIntent().getStringExtra("token");
        price = getIntent().getStringExtra("price");

        final Gson gson = new Gson();
        RequestBody requestBody;

        v = findViewById(android.R.id.content);
//        JsonObject objCust = new JsonObject();
//        objCust.addProperty("id","dFhtU9wM");
//
//        String someObj = gson.toJson(objCust);
        Log.d(TAG, "The objKey: " + token);

        requestBody = RequestBody.create(JSON,token);
        Request request = new Request.Builder()
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

//    /**
//     * This method is called if createCustomer() is successful in creating the new customer
//     * It then generates the client token needed and passes to onBraintreeSubmit()
//     * @param customer
//     */
//    public void getToken(String customer) {
//
//         RequestBody requestBody = RequestBody.create(JSON,customer);
//         Request request = new Request.Builder()
//                 .url("http://10.0.2.2:8383/client_token_withCred")
//                 .post(requestBody)
//                 .build();
//
//         GetHttp data = new GetHttp(request);
//         data.setGetRespListener(new GetHttp.GetRespListener() {
//             @Override
//             public void r(Response res) {
//                 if ( res != null ) {
//                     try {
//                         JSONObject jObj = new JSONObject(res.body().string());
//                         JSONObject jObj2 = jObj.getJSONObject("token");
//                         Log.d(TAG, "Made it in getToken with jObj: " + jObj2.get("success"));
//                         if ( jObj2.get("success").equals(false) ) {
//                             Log.d(TAG, "Something went wrong!");
//                         }
//
//                         clientToken = jObj2.getString("clientToken");
//                         Log.d(TAG, "Leaving getToken with clientToken " + clientToken);
//
//                         onBraintreeSubmit(clientToken, v);
//                     } catch ( JSONException | IOException e ) {
//                         e.printStackTrace();
//                     }
//                 }
//             }
//         });
//    }

//    /**
//     * This method creates a new customer if the first call to create a customer fails
//     */
//    public void createCustomer() {
//        final Gson gson = new Gson();
//        RequestBody requestBody;
//        JsonObject objCust = new JsonObject();
//        objCust.addProperty("id","dFhtU9wM");
//        objCust.addProperty("firstName","Angela");
//        objCust.addProperty("lastName", "Marsto");
//        objCust.addProperty("email","amarsto@blahsae.com");
//
//        String custObj = gson.toJson(objCust);
//
//        Log.d(TAG, "The result after gson: " + custObj);
//
//        requestBody = RequestBody.create(JSON, custObj);
//        Request request = new Request.Builder()
//                .url("http://10.0.2.2:8383/create_client_withCred")
//                .post(requestBody)
//                .build();
//
//        GetHttp createUser = new GetHttp(request);
//        createUser.setGetRespListener(new GetHttp.GetRespListener() {
//            @Override
//            public void r(Response res) {
//                if ( res != null ) {
//                    try {
//                        Log.d(TAG, "The result createCustomer is: " + res.toString());
//                        JSONObject jsonObject = new JSONObject(res.body().string());
//                        JSONObject jsonObject1 = jsonObject.getJSONObject("token");
//
//                        JSONObject jsonObject2 = jsonObject1.getJSONObject("customer");
//                        Log.d(TAG, "The result createCustomer is: " + jsonObject2.toString());
//                        String customer = gson.toJson(jsonObject2);
//                        getToken(customer);
//                    } catch ( IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

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
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                Gson gson = new GsonBuilder().serializeNulls().create();

                isCreated = false;

                OkHttpClient client = new OkHttpClient();

                Log.d(TAG, "This is the payment info: " + result.getPaymentMethodNonce() + " json object: "  + result.toString());
                final String c = gson.toJson(result);

                Log.d(TAG, "The total result: " + c);

                final JsonObject newPayment = new JsonObject();
                newPayment.addProperty("id","dFhtU9wM");
                newPayment.addProperty("paymentMethodNonce", c);

                String d = gson.toJson(newPayment);

                final RequestBody requestBody = RequestBody.create(JSON, d);
                final Request request = new Request.Builder()
                        .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/update_client")
                        .post(requestBody)
                        .build();
                GetHttp updateClient = new GetHttp(request);
                updateClient.setGetRespListener(new GetHttp.GetRespListener() {
                    @Override
                    public void r(Response res) {
                        Gson gson = new Gson();
                        Log.d(TAG, "The response on update: " + res);
                        JsonObject opt = new JsonObject();
                        opt.addProperty("submitForSettlement",true);
                        String options = gson.toJson(opt);
                        JsonObject j = new JsonObject();
                        Log.d(TAG, "The c " + c);
                        j.addProperty("amount", price);
                        j.addProperty("paymentMethodNonce", c);
                        j.addProperty("options",options);
                        String payment = gson.toJson(j);
                        RequestBody req = RequestBody.create(JSON,payment);
                        makeSale(req);
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
     * @param requestBody
     */
    public void makeSale(RequestBody requestBody) {
        OkHttpClient client = new OkHttpClient();
        final Request requestTrans = new Request.Builder()
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/sale")
                .post(requestBody)
                .build();

//        GetHttp commitTransaction = new GetHttp(requestTrans);
//        commitTransaction.setGetRespListener(new GetHttp.GetRespListener() {
//            @Override
//            public void r(Response res) {
//                Log.d(TAG, "This is the response in sale " + res);
//                if ( res.code() == 200 ) {
//                    goHome();
//                } else {
//
//                }
////
//            }
//        });

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
        i.putExtra("token",token);
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



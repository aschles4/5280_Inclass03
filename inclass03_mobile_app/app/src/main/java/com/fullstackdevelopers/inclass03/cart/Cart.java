package com.fullstackdevelopers.inclass03.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

import java.io.IOException;

public class Cart extends AppCompatActivity implements BraintreePaymentResultListener, BraintreeErrorListener,
        BraintreeCancelListener, BraintreeListener {

    private static final int REQUEST_CODE = 200;
    private static final int RESULT_OK = -1;
    private BraintreeFragment mBraintreeFragment;
    private String mAuthorization;
    private static final String TAG = "Cart";
    private String key;
    private View v;
    private String clientToken;
    private boolean isCreated;
    private boolean exists;
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Gson gson = new Gson();
        RequestBody requestBody;

//        final OkHttpClient client = new OkHttpClient();
        v = findViewById(android.R.id.content);
        JsonObject objCust = new JsonObject();
        objCust.addProperty("id","AzxirstajlRHy");

        String someObj = gson.toJson(objCust);

        Log.d(TAG, "The objKey: " + someObj);

        boolean custExists = findCust(someObj);

//        requestBody = RequestBody.create(JSON,someObj);
//        Request request = new Request.Builder()
//                .url("http://10.0.2.2:8383/client_token_withCred")
//                .post(requestBody)
//                .build();
//
//        GetHttp data = new GetHttp(request);
//        data.setGetRespListener(new GetHttp.GetRespListener() {
//            @Override
//            public void r(Response res) {
//                if ( res != null ) {
//                    try {
//                        JSONObject jObj = new JSONObject(res.body().string());
//                        JSONObject jObj2 = jObj.getJSONObject("token");
//                        Log.d(TAG, "Made it in with jObj: " + jObj2.toString());
//                        clientToken = jObj2.getString("clientToken");
//                        Log.d(TAG, "specific object: " + clientToken);
//
////                        checkUserPaymentExists(clientToken);
//
//
//
//                        onBraintreeSubmit(clientToken, v);
//                    } catch ( JSONException | IOException e ) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }

    public boolean findCust(final String someObj) {
        exists = false;
        RequestBody requestBody = RequestBody.create(JSON,someObj);
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8383/find_customer")
                .post(requestBody)
                .build();
        GetHttp custExist = new GetHttp(request);
        custExist.setGetRespListener(new GetHttp.GetRespListener() {
            @Override
            public void r(Response res) {
                try {
//                    Gson gson = new Gson();
                    String cus = res.body().string();
                    Log.d(TAG, "The result is: " + cus);
                    exists = true;
                } catch ( IOException e ) {
                    Log.d(TAG, "Something went wrong looking for client! ");
                }

            }
        });

        return exists;
    }

    public void checkUserPaymentExists(final String clientToken) {

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
                    Log.d(TAG, "Here is the name! " + name);

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
                .clientToken(key)
                .vaultManager(true)
                .cardholderNameStatus(CardForm.FIELD_REQUIRED);
        Log.d(TAG, "Made it inside onBraintreeSubmit");
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Inside onActivityResult" + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                RequestBody requestBody;
                Gson gson = new GsonBuilder().serializeNulls().create();

                isCreated = false;

                OkHttpClient client = new OkHttpClient();




                Log.d(TAG, "This is the payment info: " + result.getPaymentMethodNonce() + " json object: "  + result.toString());
                String c = gson.toJson(result);
                String d = gson.toJson(result.getPaymentMethodNonce());
                Log.d(TAG, "The total result: " + c + "\nthe result of just mPaymentMethodNonce " + d);



                JsonObject objCust = new JsonObject();
                objCust.addProperty("id","AzxirstajlRHy2");
                objCust.addProperty("firstName","Joey");
                objCust.addProperty("lastName", "Savoy");
                objCust.addProperty("mMethodPaymentNonce",c);

                String custObj = gson.toJson(objCust);

                Log.d(TAG, "The result after gson: " + custObj);

                requestBody = RequestBody.create(JSON, custObj);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8383/create_client_withCard")
                        .post(requestBody)
                        .build();

                GetHttp createUser = new GetHttp(request);
                createUser.setGetRespListener(new GetHttp.GetRespListener() {
                    @Override
                    public void r(Response res) {
                        if ( res != null ) {
                            try {
                                Log.d(TAG, "The result from customer create: " + res.body().string());
                                isCreated = true;
                            } catch ( IOException e ) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                if ( isCreated ) {


                    String obj = gson.toJson(result);

                    requestBody = RequestBody.create(JSON, obj);
                    final Request requestTrans = new Request.Builder()
                            .url("http://10.0.2.2:8383/sale")
                            .post(requestBody)
                            .build();

                    GetHttp commitTransaction = new GetHttp(request);
                    commitTransaction.setGetRespListener(new GetHttp.GetRespListener() {
                        @Override
                        public void r(Response res) {
                            Log.d(TAG, "This is the response " + res);
                        }
                    });
//                    client.newCall(requestTrans).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        @Override
//                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                            Log.d(TAG, "This is the response " + response + " Call: " + call);
//                        }
//                    });
                }



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



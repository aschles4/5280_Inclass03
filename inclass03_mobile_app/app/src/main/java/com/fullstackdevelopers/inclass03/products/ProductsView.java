package com.fullstackdevelopers.inclass03.products;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.fullstackdevelopers.inclass03.HomeActivity;
import com.fullstackdevelopers.inclass03.R;
import com.fullstackdevelopers.inclass03.cart.CartView;
import com.fullstackdevelopers.inclass03.data.Cart;
import com.fullstackdevelopers.inclass03.data.Product;
import com.fullstackdevelopers.inclass03.profile.ProfileView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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

import static com.estimote.coresdk.common.config.EstimoteSDK.getApplicationContext;

public class ProductsView extends Fragment implements ProductsAdapter.OnProductListener {
    private OnFragmentInteractionListener mListener;
    private View view;
    private String token;
    private final String TAG = "ProductsView";

    public ProductsView() {
    }

    //Not sure what to do with Token, but got it
    public ProductsView(String token) {
        this.token = token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        beacons();
        bottomNav();
        Cart cart = getCart(getContext());
        cartValue(cart);
//        Log.d("cart", cart.toString());
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
        Cart cart = getCart(view.getContext());
        cartValue(cart);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void bottomNav() {
        view.findViewById(R.id.nav_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartView p = new CartView(token);
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, p, "tag_cart_view")
                        .addToBackStack("tag_products_view")
                        .commit();
            }
        });

        view.findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileView p = new ProfileView(token);
                getFragmentManager().beginTransaction()
                        .replace(R.id.home_layout, p, "tag_profile_view")
                        .addToBackStack("tag_products_view")
                        .commit();

            }
        });
        view.findViewById(R.id.nav_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment l = getFragmentManager().findFragmentByTag("tag_login");
                Log.d("signup", "Return to login after frag l" + l);
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, l)
                        .addToBackStack("tag_signup")
                        .commit();
            }
        });
    }

    //Initialze the RecyclerView and Currently uses a test data set
    public void productList(ArrayList<Product> products) {
        RecyclerView recyclerView;
        RecyclerView.Adapter mAdapter;
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView = view.findViewById(R.id.productsList);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ProductsAdapter(products, ProductsView.this);
        recyclerView.setAdapter(mAdapter);
    }

    //If no Cart exists create one
    public void initCart(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    //getCart from localstorage
    public Cart cartFromLocalStorage(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cart cart = (Cart) ois.readObject();
        cartValue(cart);
        return cart;
    }

    //moved code here to look cleaner because of try/catch blocks
    public Cart getCart(Context context) {
        Cart cart = null;
        try {
            cart = cartFromLocalStorage(context, "CART");
        } catch (Exception e) {
            ArrayList<Product> products = new ArrayList<>();
            try {
                initCart(context, "CART", new Cart(UUID.randomUUID().toString(), "UserID", products, 0));
                cart = cartFromLocalStorage(context, "CART");
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return cart;
    }

    //update the badge over the cart based on cart.size()
    public void cartValue(Cart cart) {
        ImageView badge = view.findViewById(R.id.cartSize);
        TextView cartValue = view.findViewById(R.id.cartValue);
        ArrayList<Product> products = cart.getProducts();
        int totalProducts = products.size();
        if (totalProducts != 0) {
            badge.setVisibility(View.VISIBLE);
            cartValue.setText(String.valueOf(totalProducts));
        }
    }

    public void getProducts(String type) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final Gson gson = new Gson();
        final JsonObject selectType = new JsonObject();
        selectType.addProperty("type", type);
        String d = gson.toJson(selectType);
        final RequestBody requestBody = RequestBody.create(JSON, d);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://ooelz49nm4.execute-api.us-east-1.amazonaws.com/default/findProducts")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("testFail", e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject prods = new JSONObject(response.body().string());
                    JSONArray prodFromDB = prods.getJSONArray("products");
                    final ArrayList<Product> products = new ArrayList<>();

                    Log.d(TAG, "This is the products array: " + products.toString());

                    for (int i = 0; i < prodFromDB.length(); i++) {
                        Type productType = new TypeToken<Product>() {
                        }.getType();
                        Product p = gson.fromJson(prodFromDB.get(i).toString(), productType);
                        products.add(p);
                        Log.d(TAG, "This is the products array: " + products.toString());
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            productList(products);
                        }

                    });

                } catch (IOException | JSONException e) {
                    Log.d("test", e.toString());
                }
            }
        });

    }

    public void beacons() {
        EstimoteSDK.initialize(getContext(), "proximityapp-fz8", "e8c7919f9f977891f9657eb768154767");
        final BeaconManager beaconManager = new BeaconManager(getContext());
//        beaconManager.setForegroundScanPeriod(13, 10);
        final BeaconRegion region = new BeaconRegion("monitored region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            int count = 0;

            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    // TODO: update the UI here
                    Log.d(TAG, "Nearest places: " + nearestBeacon.getMeasuredPower() + count);
                    count += 1;
                    if (nearestBeacon.getMinor() == 46246) {
                        getProducts("produce");
                    }
                    if (nearestBeacon.getMajor() ==45849 ) {
                        getProducts("grocery");
                    }

                }
            }
        });
        // connects beacon manager to underlying service
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                beaconManager.startMonitoring(new BeaconRegion(
//                        "monitored region",
////                       UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
//                        null,
//                        null, null));
//            }
//
//            ;
//        });
//        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
//            @Override
//            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
//                Log.d("testRegion",region.toString());
//                Log.d("testBEACON", beacons.toString());
//                Beacon beacon = beacons.get(0);
//                if(beacon.getMinor()==46246){
//                    getProducts("produce");
//                }
//                if(beacon.getMinor()==1003 && beacon.getMinor()==1000){
//                    getProducts("grocery");
//                }
//            }
//            @Override
//            public void onExitedRegion(BeaconRegion region) {
//                // could add an "exit" notification too if you want (-:
//            }
//        });
    }

    public void createNotification(String aMessage, Context context) {
        NotificationManager notifManager = null;
        final int NOTIFY_ID = 0; // ID of notification
        String id = context.getString(R.string.default_notification_channel_id); // default_channel_id
        String title = context.getString(R.string.default_notification_channel_title); // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        builder = new NotificationCompat.Builder(context, id);
        intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        builder.setContentTitle(aMessage)                            // required
                .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                .setContentText(context.getString(R.string.app_name)) // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(aMessage)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        if (notifManager == null) {
            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);

            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }

        } else {

            builder.setPriority(Notification.PRIORITY_HIGH);

        }
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
    }

}
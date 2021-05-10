package com.app.orion_vendor.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.Constants;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.fragments.PDetailFragment;
import com.app.orion_vendor.models.Destination;
import com.app.orion_vendor.models.Order;
import com.app.orion_vendor.models.OrderItem;
import com.app.orion_vendor.models.Product;
import com.app.orion_vendor.models.Store;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverDetailActivity extends BaseActivity {

    CircleImageView pictureBox;
    TextView nameBox, statusBox, addressBox, distanceBox, submitButton;
    TextView title, label;
    ImageView contactButton, backButton;
    LinearLayout container;
    FrameLayout progressBar;
    ArrayList<OrderItem> orderItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        progressBar = (FrameLayout)findViewById(R.id.loading_bar);

        pictureBox = (CircleImageView)findViewById(R.id.pictureBox);
        nameBox = (TextView)findViewById(R.id.nameBox);
        statusBox = (TextView)findViewById(R.id.statusBox);
        addressBox = (TextView)findViewById(R.id.addressBox);
        distanceBox = (TextView)findViewById(R.id.distanceBox);
        submitButton = (TextView)findViewById(R.id.btn_submit);
        container = (LinearLayout) findViewById(R.id.container);
        label = (TextView)findViewById(R.id.lb);
        title = (TextView)findViewById(R.id.title);
        contactButton = (ImageView) findViewById(R.id.btn_contact);
        backButton = (ImageView) findViewById(R.id.btn_back);

        title.setTypeface(bold);
        nameBox.setTypeface(bold);
        statusBox.setTypeface(normal);
        addressBox.setTypeface(normal);
        distanceBox.setTypeface(normal);
        label.setTypeface(normal);
        submitButton.setTypeface(bold);

        Glide.with(getApplicationContext()).load(Commons.user.get_photoUrl()).into(pictureBox);
        nameBox.setText(Commons.user.get_name());
        statusBox.setText(Commons.user.get_status().equals("available")?"Available":"Unavailable");
        addressBox.setText(Commons.user.get_address());
        distanceBox.setText(String.valueOf(df.format(Commons.user.getDistance()/1000)) + " km");

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Commons.user.get_phone_number()));
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!orderItems.isEmpty())
                        requestPreparedOrderToDriver(createDriverOrderJsonString(orderItems));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreparedOrderItems();

    }

    public void getPreparedOrderItems() {

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "preparedOrderItems")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                orderItems.clear();
                                container.removeAllViews();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int j=0; j<dataArr.length(); j++) {
                                    JSONObject obj = (JSONObject) dataArr.get(j);
                                    OrderItem item = new OrderItem();
                                    item.setId(obj.getInt("id"));
                                    item.setOrder_id(obj.getInt("order_id"));
                                    item.setUser_id(obj.getInt("member_id"));
                                    item.setVendor_id(obj.getInt("vendor_id"));
                                    item.setStore_id(obj.getInt("store_id"));
                                    item.setStore_name(obj.getString("store_name"));
                                    item.setProduct_id(obj.getInt("product_id"));
                                    item.setProduct_name(obj.getString("product_name"));
                                    item.setCategory(obj.getString("category"));
                                    item.setSubcategory(obj.getString("subcategory"));
                                    item.setGender(obj.getString("gender"));
                                    item.setGender_key(obj.getString("gender_key"));
                                    item.setDelivery_days(obj.getInt("delivery_days"));
                                    item.setDelivery_price(Double.parseDouble(obj.getString("delivery_price")));
                                    item.setPrice(Double.parseDouble(obj.getString("price")));
                                    item.setUnit(obj.getString("unit"));
                                    item.setQuantity(obj.getInt("quantity"));
                                    item.setDate_time(obj.getString("date_time"));
                                    item.setPicture_url(obj.getString("picture_url"));
                                    item.setStatus(obj.getString("status"));
                                    item.setOrderID(obj.getString("orderID"));
                                    item.setContact(obj.getString("contact"));
                                    item.setDiscount(obj.getInt("discount"));
                                    item.setPaid_amount(Double.parseDouble(obj.getString("paid_amount")));
                                    item.setPaid_time(obj.getString("paid_time"));
                                    item.setPayment_status(obj.getString("payment_status"));
                                    item.setPaid_id(obj.getInt("paid_id"));
                                    item.setAddress(obj.getString("address"));
                                    item.setAddress_line(obj.getString("address_line"));
                                    double lat = 0.0d, lng = 0.0d;
                                    if(obj.getString("latitude").length() > 0){
                                        lat = Double.parseDouble(obj.getString("latitude"));
                                        lng = Double.parseDouble(obj.getString("longitude"));
                                    }
                                    item.setLatLng(new LatLng(lat, lng));

                                    Log.d("STATUS!!!", item.getStatus());

                                    orderItems.add(item);

                                    LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View view = layoutInflater.inflate(R.layout.item_driver_orders, null);
                                    TextView orderIDBox = (TextView) view.findViewById(R.id.orderIDBox);
                                    TextView categoryBox = (TextView) view.findViewById(R.id.categoryBox);
                                    TextView priceBox = (TextView) view.findViewById(R.id.priceBox);
                                    TextView quantityBox = (TextView) view.findViewById(R.id.quantityBox);
                                    ImageView pictureBox = (ImageView) view.findViewById(R.id.pictureBox);
                                    TextView genderBox = (TextView) view.findViewById(R.id.genderBox);
                                    TextView addressBox = (TextView) view.findViewById(R.id.addressBox);
                                    TextView productNameBox = (TextView) view.findViewById(R.id.productNameBox);
                                    TextView statusBox = (TextView) view.findViewById(R.id.statusBox);
                                    TextView addressLineBox = (TextView) view.findViewById(R.id.addressLineBox);
                                    TextView deliveryDaysBox = (TextView) view.findViewById(R.id.deliveryDaysBox);
                                    ImageView detailButton = (ImageView) view.findViewById(R.id.btn_detail);
                                    
                                    priceBox.setTypeface(bold);
                                    quantityBox.setTypeface(bold);
                                    productNameBox.setTypeface(bold);
                                    categoryBox.setTypeface(normal);
                                    genderBox.setTypeface(normal);
                                    addressBox.setTypeface(normal);
                                    addressLineBox.setTypeface(normal);
                                    deliveryDaysBox.setTypeface(normal);
                                    orderIDBox.setTypeface(normal);
                                    statusBox.setTypeface(normal);

                                    orderIDBox.setText(item.getOrderID());
                                    priceBox.setText(df.format(item.getPrice()) + " " + Constants.currency);
                                    productNameBox.setText(item.getProduct_name());
                                    categoryBox.setText(item.getCategory() + "|" + item.getSubcategory());
                                    quantityBox.setText("QTY: " + String.valueOf(item.getQuantity()));
                                    genderBox.setText(item.getGender());
                                    addressBox.setText(item.getAddress());
                                    addressLineBox.setText(item.getAddress_line());
                                    deliveryDaysBox.setText(item.getDelivery_days() + " Days");

                                    if(item.getStatus().length() > 0){
                                        statusBox.setText(Commons.orderStatus.statusStr.get(item.getStatus()));
                                    }

                                    if(item.getPicture_url().length() > 0){
                                        Picasso.with(getApplicationContext())
                                                .load(item.getPicture_url())
                                                .into(pictureBox);
                                    }

                                    pictureBox.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            productInfo(String.valueOf(item.getProduct_id()));
                                        }
                                    });

                                    detailButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Commons.orderItem = item;
                                            getOrder(item);
                                        }
                                    });

                                    container.addView(view);

                                }

                                if(orderItems.isEmpty()){
                                    submitButton.setVisibility(View.INVISIBLE);
                                    label.setVisibility(View.INVISIBLE);
                                    showToast("No prepared order...");
                                }else {
                                    submitButton.setVisibility(View.VISIBLE);
                                    label.setVisibility(View.VISIBLE);
                                }

                            }else {
                                showToast("Server issue.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void getOrder(OrderItem orderItem){

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "orderById")
                .addBodyParameter("order_id", String.valueOf(orderItem.getOrder_id()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if (result.equals("0")) {
                                JSONObject object = response.getJSONObject("data");
                                Order order = new Order();
                                order.setId(object.getInt("id"));
                                order.setUser_id(object.getInt("member_id"));
                                order.setOrderID(object.getString("orderID"));
                                order.setPrice(Double.parseDouble(object.getString("price")));
                                order.setUnit(object.getString("unit"));
                                order.setShipping(Double.parseDouble(object.getString("shipping")));
                                order.setDate(object.getString("date_time"));
                                order.setEmail(object.getString("email"));
                                order.setAddress(object.getString("address"));
                                order.setAddress_line(object.getString("address_line"));
                                order.setPhone_number(object.getString("phone_number"));
                                order.setStatus(object.getString("status"));
                                order.setDiscount(object.getInt("discount"));
                                double lat = 0.0d, lng = 0.0d;
                                if(object.getString("latitude").length() > 0){
                                    lat = Double.parseDouble(object.getString("latitude"));
                                    lng = Double.parseDouble(object.getString("longitude"));
                                }
                                order.setLatLng(new LatLng(lat, lng));

                                ArrayList<OrderItem> orderItems = new ArrayList<>();
                                JSONArray objArr = object.getJSONArray("items");
                                for(int j=0; j<objArr.length(); j++) {
                                    JSONObject obj = (JSONObject) objArr.get(j);
                                    OrderItem item = new OrderItem();
                                    item.setId(obj.getInt("id"));
                                    item.setOrder_id(obj.getInt("order_id"));
                                    item.setUser_id(obj.getInt("member_id"));
                                    item.setVendor_id(obj.getInt("vendor_id"));
                                    item.setStore_id(obj.getInt("store_id"));
                                    item.setStore_name(obj.getString("store_name"));
                                    item.setProduct_id(obj.getInt("product_id"));
                                    item.setProduct_name(obj.getString("product_name"));
                                    item.setCategory(obj.getString("category"));
                                    item.setSubcategory(obj.getString("subcategory"));
                                    item.setGender(obj.getString("gender"));
                                    item.setGender_key(obj.getString("gender_key"));
                                    item.setDelivery_days(obj.getInt("delivery_days"));
                                    item.setDelivery_price(Double.parseDouble(obj.getString("delivery_price")));
                                    item.setPrice(Double.parseDouble(obj.getString("price")));
                                    item.setUnit(obj.getString("unit"));
                                    item.setQuantity(obj.getInt("quantity"));
                                    item.setDate_time(obj.getString("date_time"));
                                    item.setPicture_url(obj.getString("picture_url"));
                                    item.setStatus(obj.getString("status"));
                                    item.setOrderID(obj.getString("orderID"));
                                    item.setContact(obj.getString("contact"));
                                    item.setDiscount(obj.getInt("discount"));
                                    item.setPaid_amount(Double.parseDouble(obj.getString("paid_amount")));
                                    item.setPaid_time(obj.getString("paid_time"));
                                    item.setPayment_status(obj.getString("payment_status"));
                                    item.setPaid_id(obj.getInt("paid_id"));
                                    item.setAddress(obj.getString("address"));
                                    item.setAddress_line(obj.getString("address_line"));
                                    lat = 0.0d; lng = 0.0d;
                                    if(object.getString("latitude").length() > 0){
                                        lat = Double.parseDouble(object.getString("latitude"));
                                        lng = Double.parseDouble(object.getString("longitude"));
                                    }
                                    item.setLatLng(new LatLng(lat, lng));

                                    Log.d("STATUS!!!", item.getStatus());

                                    orderItems.add(item);
                                }

                                order.setItems(orderItems);

                                Commons.order = order;
                                Intent intent = new Intent(getApplicationContext(), OrderDetailActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });

    }

    private void productInfo(String productId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "productInfo")
                .addBodyParameter("product_id", productId)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                JSONObject object = response.getJSONObject("product");
                                Product product = new Product();
                                product.setIdx(object.getInt("id"));
                                product.setStoreId(object.getInt("store_id"));
                                product.setUserId(object.getInt("member_id"));
                                product.setBrandId(object.getInt("brand_id"));
                                product.setName(object.getString("name"));
                                product.setPicture_url(object.getString("picture_url"));
                                product.setCategory(object.getString("category"));
                                product.setSubcategory(object.getString("subcategory"));
                                product.setGender(object.getString("gender"));
                                product.setGenderKey(object.getString("gender_key"));
                                product.setPrice(Double.parseDouble(object.getString("price")));
                                product.setNew_price(Double.parseDouble(object.getString("new_price")));
                                product.setUnit(object.getString("unit"));
                                product.setDescription(object.getString("description"));
                                product.setRegistered_time(object.getString("registered_time"));
                                product.setStatus(object.getString("status"));
                                product.setBrand_name(object.getString("brand_name"));
                                product.setBrand_logo(object.getString("brand_logo"));
                                product.setDelivery_price(Double.parseDouble(object.getString("delivery_price")));
                                product.setDelivery_days(Integer.parseInt(object.getString("delivery_days")));

                                product.setLikes(object.getInt("likes"));
                                product.setRatings(Float.parseFloat(object.getString("ratings")));


                                object = response.getJSONObject("store");
                                Store store = new Store();
                                store.setId(object.getInt("id"));
                                store.setUserId(object.getInt("member_id"));
                                store.setName(object.getString("name"));
                                store.setPhoneNumber(object.getString("phone_number"));
                                store.setAddress(object.getString("address"));
                                store.setRatings(Float.parseFloat(object.getString("ratings")));
                                store.setReviews(object.getInt("reviews"));
                                store.setLogoUrl(object.getString("logo_url"));
                                store.set_registered_time(object.getString("registered_time"));
                                store.set_status(object.getString("status"));
                                double lat = 0.0d, lng = 0.0d;
                                if(object.getString("latitude").length() > 0){
                                    lat = Double.parseDouble(object.getString("latitude"));
                                    lng = Double.parseDouble(object.getString("longitude"));
                                }
                                store.setLatLng(new LatLng(lat, lng));

                                Commons.product1 = product;
                                Commons.store1 = store;

                                Fragment fragment = new PDetailFragment();
                                FragmentManager manager = getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.activity, fragment);
                                transaction.addToBackStack(null).commit();

                            }else {
                                showToast("Server issue.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    String itemsStr = "";

    public String createDriverOrderJsonString(ArrayList<OrderItem> items)throws JSONException{

        itemsStr = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        if (items.size()>0){
            for(OrderItem item:items){

                String itemId = String.valueOf(item.getId());

                jsonObj=new JSONObject();

                try {
                    jsonObj.put("item_id", itemId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArr.put(jsonObj);
            }
            JSONObject scheduleObj = new JSONObject();
            scheduleObj.put("itemIds", jsonArr);
            itemsStr = scheduleObj.toString();
            return itemsStr;
        }

        return itemsStr;
    }

    public void requestPreparedOrderToDriver(final String itemsStr) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "requestPreparedOrderToDriver")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("driver_id", String.valueOf(Commons.user.get_idx()))
                .addBodyParameter("itemsStr", itemsStr)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){

                                showAlertDialog("Info", "Your request has been submitted to the driver. Pleas wait for him to accept it.",
                                        DriverDetailActivity.this, new Callable<Void>() {
                                            @Override
                                            public Void call() throws Exception {
                                                toHome();
                                                return null;
                                            }
                                        });

                            }else if(result.equals("2")){
                                showAlertDialog("Info", "You have already requested the same order to the driver" +
                                                " and it is on processing still by the driver.\nPlease wait...",
                                        DriverDetailActivity.this, new Callable<Void>() {
                                            @Override
                                            public Void call() throws Exception {
                                                toHome();
                                                return null;
                                            }
                                        });
                            }else if(result.equals("3")){
                                showAlertDialog("Info", "The driver has already rejected the order." +
                                                " Please choose another driver.",
                                        DriverDetailActivity.this, new Callable<Void>() {
                                            @Override
                                            public Void call() throws Exception {
                                                toHome();
                                                return null;
                                            }
                                        });
                            }else {
                                showToast("Error");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });

    }

    private void toHome(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }


    public void toDriverLocation(View view){
        Commons.orderItem = null;
        Destination destination = new Destination();
        destination.setUserId(Commons.user.get_idx());
        destination.setTitle(Commons.user.get_name());
        destination.setPicture_url(Commons.user.get_photoUrl());
        destination.setAddress(Commons.user.get_address());
        destination.setLatLng(Commons.user.getLatLng());
        Commons.destination = destination;
        Intent intent = new Intent(getApplicationContext(), LocationTrackingActivity.class);
        startActivity(intent);
    }

}




































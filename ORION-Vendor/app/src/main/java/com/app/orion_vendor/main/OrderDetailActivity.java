package com.app.orion_vendor.main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import com.app.orion_vendor.models.OrderItem;
import com.app.orion_vendor.models.Product;
import com.app.orion_vendor.models.Store;
import com.app.orion_vendor.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class OrderDetailActivity extends BaseActivity {

    TextView title, orderIDBox, orderDateBox, phoneBox, addressBox, addressLineBox, subTotalBox, shippingBox, totalBox, bonusBox, priceBox, deliveryBox;
    FrameLayout trackFrame, oldPriceFrame;
    TextView paymentStatusBox;
    LinearLayout bonusFrame;
    ImageView driverButton;
    RoundedImageView pictureBox;
    TextView productNameBox, storeNameBox, categoryBox, quantityBox, statusBox;
    TextView progressButton, cancelButton;
    FrameLayout progressBar;
    public static DecimalFormat df = new DecimalFormat("0.00");
    ImageView[] nodes = {};
    View[] lines = {};
    double deliveryPrice = 0.0d;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        ((TextView)findViewById(R.id.title)).setTypeface(bold);
        ((TextView)findViewById(R.id.caption1)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption2)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption3)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption4)).setTypeface(normal);

        ((TextView)findViewById(R.id.caption5)).setTypeface(bold);
        ((TextView)findViewById(R.id.caption6)).setTypeface(bold);
        ((TextView)findViewById(R.id.caption7)).setTypeface(bold);
        ((TextView)findViewById(R.id.caption8)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption9)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption10)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption11)).setTypeface(normal);
        ((TextView)findViewById(R.id.caption12)).setTypeface(bold);
        ((TextView)findViewById(R.id.caption13)).setTypeface(normal);

        progressBar = (FrameLayout) findViewById(R.id.loading_bar);
        title = (TextView)findViewById(R.id.title);

        driverButton = (ImageView)findViewById(R.id.btn_driver);

        trackFrame = (FrameLayout)findViewById(R.id.trackFrame);
        orderIDBox = (TextView)findViewById(R.id.order_number);
        orderDateBox = (TextView)findViewById(R.id.order_date);
        phoneBox = (TextView)findViewById(R.id.phone);
        addressBox = (TextView)findViewById(R.id.address);
        addressLineBox = (TextView)findViewById(R.id.address2);
        subTotalBox = (TextView)findViewById(R.id.subtotal_price);
        shippingBox = (TextView)findViewById(R.id.shipping_price);
        totalBox = (TextView)findViewById(R.id.total_price);

        bonusFrame = (LinearLayout)findViewById(R.id.bonusFrame);
        bonusBox = (TextView)findViewById(R.id.bonus);

        paymentStatusBox = (TextView)findViewById(R.id.payment_status);
        paymentStatusBox.setTypeface(bold);

        if(Commons.orderItem.getDiscount() > 0)bonusFrame.setVisibility(View.VISIBLE);

        productNameBox = (TextView)findViewById(R.id.productNameBox);
        storeNameBox = (TextView)findViewById(R.id.storeNameBox);
        categoryBox = (TextView)findViewById(R.id.categoryBox);
        quantityBox = (TextView)findViewById(R.id.quantityBox);
        statusBox = (TextView)findViewById(R.id.statusBox);

        productNameBox.setTypeface(bold);
        storeNameBox.setTypeface(normal);
        categoryBox.setTypeface(normal);
        quantityBox.setTypeface(bold);
        statusBox.setTypeface(normal);

        progressButton = (TextView)findViewById(R.id.btn_progress);
        cancelButton = (TextView)findViewById(R.id.btn_cancel);

        if(Commons.orderItem.getPayment_status().equals("pay")){
            if(Commons.orderItem.getStatus().equals("delivered")){
                progressButton.setText("Completed");
            }else {
                progressButton.setText(Commons.orderStatus.nextStatusStr.get(Commons.orderItem.getStatus()));
            }
            driverButton.setVisibility(View.VISIBLE);
        }
        else {
            if(Commons.orderItem.getStatus().equals("delivered")){
                progressButton.setText("Delivered");
                driverButton.setVisibility(View.VISIBLE);
            }else {
                if(Commons.orderItem.getStatus().equals("ready")){
                    driverButton.setVisibility(View.VISIBLE);
                }else {
                    driverButton.setVisibility(View.GONE);
                }
                progressButton.setText(Commons.orderStatus.nextStatusStr.get(Commons.orderItem.getStatus()));
            }
        }

        if(Commons.orderItem.getStatus().equals("delivered")){
            cancelButton.setVisibility(View.GONE);
            progressButton.setBackgroundResource(R.drawable.green_round_stroke);
            progressButton.setTextColor(getColor(R.color.green));
            progressButton.setTypeface(bold);
        }

        if(Commons.orderItem.getPayment_status().equals("pay")){
            paymentStatusBox.setText("(PAID)");
        }
        else {
            paymentStatusBox.setText("(UNPAID)");
            paymentStatusBox.setTextColor(Color.GRAY);
        }

        pictureBox = (RoundedImageView)findViewById(R.id.pictureBox);

        Picasso.with(getApplicationContext()).load(Commons.orderItem.getPicture_url()).into(pictureBox);

        productNameBox.setText(Commons.orderItem.getProduct_name());
        storeNameBox.setText(Commons.orderItem.getStore_name());
        categoryBox.setText(Commons.orderItem.getCategory());
        quantityBox.setText("X " + String.valueOf(Commons.orderItem.getQuantity()));

        priceBox = (TextView)findViewById(R.id.priceBox);
        priceBox.setText(String.valueOf(Commons.orderItem.getPrice()) + " " + Constants.currency);
        priceBox.setTypeface(normal);

        deliveryBox = (TextView)findViewById(R.id.deliveryBox);
        deliveryBox.setTypeface(bold);
        deliveryBox.setText(String.valueOf(Commons.orderItem.getDelivery_days()) + " Days, " + String.valueOf(Commons.orderItem.getDelivery_price()) + " SGD");

        deliveryPrice = Commons.orderItem.getDelivery_price();

        statusBox.setText(Commons.orderStatus.statusStr.get(Commons.orderItem.getStatus()));

        orderIDBox.setText(Commons.order.getOrderID());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
        String date = dateFormat.format(new Date(Long.parseLong(Commons.order.getDate())));
        orderDateBox.setText(date);
        phoneBox.setText(Commons.order.getPhone_number());
        addressBox.setText(Commons.order.getAddress());
        addressLineBox.setText(Commons.order.getAddress_line());

        deliveryPrice = Commons.orderItem.getDelivery_price();

        double subTotalPrice = (Commons.orderItem.getPrice()*(1- Commons.orderItem.getDiscount()/100))*Commons.orderItem.getQuantity();
        subTotalBox.setText(df.format(subTotalPrice) + " " + Constants.currency);
        bonusBox.setText("-" + df.format(Commons.orderItem.getDiscount()) + "%");
        shippingBox.setText(String.valueOf(deliveryPrice) + " " + Constants.currency);
        totalBox.setText(df.format(subTotalPrice + deliveryPrice) + " " + Constants.currency);

        setupUI(findViewById(R.id.activity), this);

        initTrackFrame();
        appearTrackFrame(Commons.orderStatus.statusIndex.get(Commons.orderItem.getStatus()));

    }

    public void contact(View view){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Commons.orderItem.getContact()));
        startActivity(intent);
    }

    public void toDriver(View view){

        getDriver(String.valueOf(Commons.orderItem.getId()));

    }

    public void back(View view){
        onBackPressed();
    }

    private void initTrackFrame(){
        nodes = new ImageView[]{
                ((ImageView) findViewById(R.id.img_placed)),
                ((ImageView)findViewById(R.id.img_confirmed)),
                ((ImageView)findViewById(R.id.img_prepared)),
                ((ImageView)findViewById(R.id.img_ready)),
                ((ImageView)findViewById(R.id.img_delivered))
        };

        lines = new View[]{
                ((View)findViewById(R.id.view_confirm)),
                ((View)findViewById(R.id.view_prepare)),
                ((View)findViewById(R.id.view_ready)),
                ((View)findViewById(R.id.view_delivery))
        };
    }

    private void appearTrackFrame(int sel){
        for(ImageView node:nodes)node.setImageResource(R.drawable.gray_circle);
        for(View line:lines)line.setBackgroundColor(getColor(R.color.gray));
        for(int i=0;i<nodes.length; i++){
            if(i <= sel){
                nodes[i].setImageResource(R.drawable.marroon_circle);
            }
            if(i == 0)continue;
            else if(i <= sel)lines[i - 1].setBackgroundColor(getColor(R.color.colorPrimary));
        }
    }

    public void cancelItem(View view){
        cancelOrderItem(Commons.orderItem);
    }

    private void cancelOrderItem(OrderItem orderItem){
        showAlertDialogForQuestion("Warning", "Are you sure you want to cancel this order?", this, null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                progressBar.setVisibility(View.VISIBLE);
                AndroidNetworking.post(ReqConst.SERVER_URL + "cancelOrderItem")
                        .addBodyParameter("item_id", String.valueOf(orderItem.getId()))
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
                                        showToast("The item has been canceled.");
                                        finish();
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

                return null;
            }
        });
    }

    public void progressOrderItem(View view){
        if(!Commons.orderItem.getStatus().equals("delivered")){
            if(Commons.orderItem.getStatus().equals("prepared")){
                Intent intent = new Intent(getApplicationContext(), DriverListActivity.class);
                startActivity(intent);
            }else {
                progressOrderItem(Commons.orderItem, Commons.orderStatus.nextStatus.get(Commons.orderItem.getStatus()));
            }
        }
    }

    public void progressOrderItem(OrderItem orderItem, String next){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "progressOrderItem")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("item_id", String.valueOf(orderItem.getId()))
                .addBodyParameter("next", next)
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
                                    item.setAddress(obj.getString("address"));
                                    item.setAddress_line(obj.getString("address_line"));
                                    double lat = 0.0d, lng = 0.0d;
                                    if(obj.getString("latitude").length() > 0){
                                        lat = Double.parseDouble(obj.getString("latitude"));
                                        lng = Double.parseDouble(obj.getString("longitude"));
                                    }
                                    item.setLatLng(new LatLng(lat, lng));

                                    if(item.getId() == Commons.orderItem.getId()){
                                        Commons.orderItem = item;
                                        appearTrackFrame(Commons.orderStatus.statusIndex.get(item.getStatus()));
                                        progressButton.setText(Commons.orderStatus.nextStatusStr.get(item.getStatus()));
                                        statusBox.setText(Commons.orderStatus.statusStr.get(item.getStatus()));
                                        if(item.getStatus().equals("delivered")){
                                            cancelButton.setVisibility(View.GONE);
                                            progressButton.setBackgroundResource(R.drawable.green_round_stroke);
                                            progressButton.setTextColor(getColor(R.color.colorPrimary));
                                        }
                                        return;
                                    }
                                }

                            }else if(result.equals("1")){
                                showToast("The customer has already canceled this order.");
                                onResume();
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

    public void getProduct(View view){
        getProductInfo(Commons.orderItem);
    }

    private void getProductInfo(OrderItem orderItem){
        getProduct(String.valueOf(orderItem.getProduct_id()));
    }

    private void getProduct(String productId) {
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

    public void getDriver(String itemId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getDriver")
                .addBodyParameter("item_id", itemId)
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
                                JSONObject object = response.getJSONObject("data");
                                User user = new User();
                                user.set_idx(object.getInt("id"));
                                user.set_name(object.getString("name"));
                                user.set_email(object.getString("email"));
                                user.set_password(object.getString("password"));
                                user.set_photoUrl(object.getString("picture_url"));
                                user.set_registered_time(object.getString("registered_time"));
                                user.setRole(object.getString("role"));
                                user.set_phone_number(object.getString("phone_number"));
                                user.set_address(object.getString("address"));
                                user.set_country(object.getString("country"));
                                user.set_area(object.getString("area"));
                                user.set_street(object.getString("street"));
                                user.set_house(object.getString("house"));
                                double lat = 0.0d, lng = 0.0d;
                                if(object.getString("latitude").length() > 0){
                                    lat = Double.parseDouble(object.getString("latitude"));
                                    lng = Double.parseDouble(object.getString("longitude"));
                                }
                                user.setLatLng(new LatLng(lat, lng));
                                user.set_stores(object.getInt("stores"));
                                user.set_status(object.getString("status"));

                                Destination destination = new Destination();
                                destination.setUserId(user.get_idx());
                                destination.setTitle(user.get_name());
                                destination.setPicture_url(user.get_photoUrl());
                                destination.setAddress(user.get_address());
                                destination.setLatLng(user.getLatLng());
                                Commons.destination = destination;
                                Intent intent = new Intent(getApplicationContext(), LocationTrackingActivity.class);
                                startActivity(intent);

                            }else {
                                showToast("No driver...");
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

}



























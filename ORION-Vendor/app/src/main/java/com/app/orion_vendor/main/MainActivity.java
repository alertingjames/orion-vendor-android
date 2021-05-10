package com.app.orion_vendor.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.base.BaseFragmentActivity;
import com.app.orion_vendor.classes.CustomTypefaceSpan;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.fragments.HelpFragment;
import com.app.orion_vendor.fragments.MyStoreFragment;
import com.app.orion_vendor.fragments.NotificationsFragment;
import com.app.orion_vendor.fragments.ProfileFragment;
import com.app.orion_vendor.fragments.RegisterStoreFragment;
import com.app.orion_vendor.fragments.VendorSettingsFragment;
import com.app.orion_vendor.models.OrderItem;
import com.bumptech.glide.Glide;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends BaseFragmentActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawer;
    NavigationView navigationView;
    FrameLayout countFrame, orderCountFrame;
    TextView countBox, orderCountBox;
    LinearLayout notiFrame, notiLayout;
    public AVLoadingIndicatorView progressBar;

    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Commons.mainActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView title = (TextView)findViewById(R.id.title);
        title.setTypeface(bold);

        countFrame = (FrameLayout)findViewById(R.id.countFrame);
        countBox = (TextView)findViewById(R.id.countBox);

        orderCountFrame = (FrameLayout)findViewById(R.id.orderCountMark);
        orderCountBox = (TextView)findViewById(R.id.orderCountBox);

        notiFrame = (LinearLayout)findViewById(R.id.notiFrame);
        notiLayout = (LinearLayout)findViewById(R.id.notiLayout);

        changeMenuFonts();
        setupUI(findViewById(R.id.activity), this);

        if(Commons.thisUser.get_stores() == 0){
            fragment = new RegisterStoreFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.output, fragment);
            transaction.commit();
        }else {
            fragment = new MyStoreFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.output, fragment);
            transaction.commit();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getNotifications();
            }
        }).start();

    }

    public void getNotifications(){
        if(notiLayout.getChildCount() > 0) notiLayout.removeAllViews();

        getCustomerNotification();
        getCustomerPaymentNotification();
        getDriverProcessNotification();
        getAdminNotification();
    }

    private void changeMenuFonts(){

        ((TextView)navigationView.getHeaderView(0).findViewById(R.id.name)).setTypeface(bold);

        Menu m = navigationView.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

    }

    private void applyFontToMenuItem(MenuItem mi) {
        int size = 16;
        float scaledSizeInPixels = size * getResources().getDisplayMetrics().scaledDensity;
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan(mi.getTitle().toString(), bold, scaledSizeInPixels), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        displaySelectedScreen(menuItem.getItemId());
        return false;
    }

    private void displaySelectedScreen(int itemId) {
        switch (itemId) {
            case R.id.store:
                if(Commons.thisUser.get_stores() == 0){
                    return;
                }
                fragment = new MyStoreFragment();
                break;
            case R.id.orders:
                if(Commons.thisUser.get_stores() == 0){
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
                startActivity(intent);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return;

            case R.id.payments:
                intent = new Intent(getApplicationContext(), PaidListActivity.class);
                startActivity(intent);
                break;

            case R.id.drivers:
                intent = new Intent(getApplicationContext(), DriverListActivity.class);
                startActivity(intent);
                break;
            case R.id.notifications:
                fragment = new NotificationsFragment();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.activity, fragment);
                transaction.addToBackStack(null).commit();

                drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return;
            case R.id.account:
                fragment = new ProfileFragment();
                manager = getSupportFragmentManager();
                transaction = manager.beginTransaction();
                transaction.replace(R.id.activity, fragment);
                transaction.addToBackStack(null).commit();

                drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return;
            case R.id.settings:
                fragment = new VendorSettingsFragment();
                manager = getSupportFragmentManager();
                transaction = manager.beginTransaction();
                transaction.replace(R.id.activity, fragment);
                transaction.addToBackStack(null).commit();

                drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return;
            case R.id.help:
                fragment = new HelpFragment();
                break;
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.output, fragment);
        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void viewMyProfile(View view){
        fragment = new ProfileFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.activity, fragment);
        transaction.addToBackStack(null).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initOrderStatus();
        getNewOrders();

        Glide.with(getApplicationContext())
                .load(Commons.thisUser.get_photoUrl())
                .into((CircleImageView)navigationView.getHeaderView(0).findViewById(R.id.avatar));
        ((TextView)navigationView.getHeaderView(0).findViewById(R.id.name)).setText(Commons.thisUser.get_name());

        if(Commons.thisUser != null){
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("HomeActivity:", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            Log.d("Token!!!", token);
                            if(token.length() > 0)
                                uploadNewToken(token);
                        }
                    });
        }

    }

    private void uploadNewToken(String token){
        AndroidNetworking.post(ReqConst.SERVER_URL + "uploadfcmtoken")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("fcm_token", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    private void getCustomerNotification(){
        Log.d("Customer NOTI!!!", String.valueOf(Commons.thisUser.get_idx()));

        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "order/" + String.valueOf(Commons.thisUser.get_idx()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myLayout = inflater.inflate(R.layout.layout_notification, null);
                    String noti = map.get("msg").toString();   Log.d("Customer Noti!!!", noti);
                    String time = map.get("date").toString();
                    String fromid = map.get("fromid").toString();
                    String fromname = map.get("fromname").toString();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);
                    noti = "Customer's new order: " + fromname;
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = dateFormat.format(new Date(Long.parseLong(time)));
                    ((TextView)myLayout.findViewById(R.id.date)).setText(date);
                    ((TextView)myLayout.findViewById(R.id.name)).setText(fromname);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    ((TextView)myLayout.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }

                            Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
                            startActivity(intent);
                        }
                    });

                    ((ImageView)myLayout.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }
                        }
                    });
                    notiLayout.addView(myLayout);
                    countFrame.setVisibility(View.VISIBLE);
                    countBox.setText(String.valueOf(notiLayout.getChildCount()));
                    ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                }catch (NullPointerException e){}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void getCustomerPaymentNotification(){

        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "pay/" + String.valueOf(Commons.thisUser.get_idx()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myLayout = inflater.inflate(R.layout.layout_notification, null);
                    String noti = map.get("msg").toString();   Log.d("Customer Noti!!!", noti);
                    String time = map.get("date").toString();
                    String fromid = map.get("fromid").toString();
                    String fromname = map.get("fromname").toString();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = dateFormat.format(new Date(Long.parseLong(time)));
                    ((TextView)myLayout.findViewById(R.id.date)).setText(date);
                    ((TextView)myLayout.findViewById(R.id.name)).setText(fromname);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    ((TextView)myLayout.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }

                            Intent intent = new Intent(getApplicationContext(), PaidListActivity.class);
                            startActivity(intent);
                        }
                    });

                    ((ImageView)myLayout.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }
                        }
                    });
                    notiLayout.addView(myLayout);
                    countFrame.setVisibility(View.VISIBLE);
                    countBox.setText(String.valueOf(notiLayout.getChildCount()));
                    ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                }catch (NullPointerException e){}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void getDriverProcessNotification(){

        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "driver_process/" + String.valueOf(Commons.thisUser.get_idx()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myLayout = inflater.inflate(R.layout.layout_notification, null);
                    String noti = map.get("msg").toString();   Log.d("Customer Noti!!!", noti);
                    String time = map.get("date").toString();
                    String fromid = map.get("fromid").toString();
                    String fromname = map.get("fromname").toString();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = dateFormat.format(new Date(Long.parseLong(time)));
                    ((TextView)myLayout.findViewById(R.id.date)).setText(date);
                    ((TextView)myLayout.findViewById(R.id.name)).setText(fromname);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    ((TextView)myLayout.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }

                            Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
                            startActivity(intent);
                        }
                    });

                    ((ImageView)myLayout.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }
                        }
                    });
                    notiLayout.addView(myLayout);
                    countFrame.setVisibility(View.VISIBLE);
                    countBox.setText(String.valueOf(notiLayout.getChildCount()));
                    ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                }catch (NullPointerException e){}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void dismissNotiFrame(){
        if(notiFrame.getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_out);
            notiFrame.setAnimation(animation);
            notiFrame.setVisibility(View.GONE);
            ((View)findViewById(R.id.notiBackground)).setVisibility(View.GONE);
        }
    }

    public void dismissNotiFrame(View view){
        dismissNotiFrame();
    }

    private void initOrderStatus(){
        Commons.orderStatus.initOrderStatus();
    }

    public void showNotiFrame(View view){
        if(notiLayout.getChildCount() > 0){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_in);
            notiFrame.setAnimation(animation);
            notiFrame.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((View)findViewById(R.id.notiBackground)).setVisibility(View.VISIBLE);
                }
            }, 200);
        }
    }

    private void getAdminNotification(){

        Firebase ref;
        ref = new Firebase(ReqConst.FIREBASE_URL + "admin/" + String.valueOf(Commons.thisUser.get_idx()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myLayout = inflater.inflate(R.layout.layout_notification, null);
                    String noti = map.get("msg").toString();
                    String time = map.get("date").toString();
                    String fromid = map.get("fromid").toString();
                    String fromname = map.get("fromname").toString();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = dateFormat.format(new Date(Long.parseLong(time)));
                    ((TextView)myLayout.findViewById(R.id.date)).setText(date);
                    ((TextView)myLayout.findViewById(R.id.name)).setText("Qhome");
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    ((TextView)myLayout.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }
                        }
                    });

                    ((ImageView)myLayout.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            dataSnapshot.getRef().removeValue();
                            notiLayout.removeView(myLayout);
                            countBox.setText(String.valueOf(notiLayout.getChildCount()));
                            ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                            if(notiLayout.getChildCount() == 0){
                                countFrame.setVisibility(View.GONE);
                                dismissNotiFrame();
                                ShortcutBadger.removeCount(getApplicationContext());
                            }
                        }
                    });
                    notiLayout.addView(myLayout);
                    countFrame.setVisibility(View.VISIBLE);
                    countBox.setText(String.valueOf(notiLayout.getChildCount()));
                    ShortcutBadger.applyCount(getApplicationContext(), notiLayout.getChildCount());
                }catch (NullPointerException e){}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void toOrders(View view){
        if(Commons.thisUser.get_stores() == 0){
            return;
        }
        Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
        startActivity(intent);
    }

    public void getNewOrders(){
        AndroidNetworking.post(ReqConst.SERVER_URL + "receivedOrderItems")
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
                                ArrayList<OrderItem> onGoingItems = new ArrayList<>();
                                ArrayList<OrderItem> placedItems = new ArrayList<>();
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

                                    Log.d("STATUS!!!", item.getStatus());

                                    if(!item.getStatus().equals("delivered")){
                                        onGoingItems.add(item);
                                        if(item.getStatus().equals("placed"))placedItems.add(item);
                                    }
                                }

                                if(onGoingItems.size() > 0){
                                    Snackbar snackbar = Snackbar
                                            .make(findViewById(R.id.activity), "You have " + String.valueOf(onGoingItems.size()) + " on-processing orders. Please check...", Snackbar.LENGTH_LONG)
                                            .setAction("GO", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent = new Intent(getApplicationContext(), OrdersActivity.class);
                                                    startActivity(intent);
                                                }
                                            });;

                                    snackbar.show();
                                }
                                if(placedItems.size() > 0) {
                                    orderCountFrame.setVisibility(View.VISIBLE);
                                    orderCountBox.setText(String.valueOf(placedItems.size()));
                                }else orderCountFrame.setVisibility(View.GONE);

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

}


































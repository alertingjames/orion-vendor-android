package com.app.orion_vendor.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.main.LocationTrackingActivity;
import com.app.orion_vendor.main.VerifyPaymentActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

public class VendorSettingsFragment extends Fragment {

    public Typeface bold, normal;
    ImageView backButton;
    TextView titleBox, label1, label2, label3, label4, label5, label6, label7;
    TextView manageButton;
    Switch notiSwitchButton;
    Switch myLocationSwitchButton, driverLocationSwitchButton, mapViewSwitchButton;

    FrameLayout progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_vendor_settings, viewGroup, false);

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        myLocationSwitchButton = (Switch)view.findViewById(R.id.locationSetting);
        driverLocationSwitchButton = (Switch)view.findViewById(R.id.driverLocationSetting);
        mapViewSwitchButton = (Switch) view.findViewById(R.id.mapviewSetting);

        if(Commons.curMapTypeIndex == 2)mapViewSwitchButton.setChecked(true);
        else mapViewSwitchButton.setChecked(false);

        if(Commons.mapCameraMoveF)myLocationSwitchButton.setChecked(true);
        else myLocationSwitchButton.setChecked(false);

        if(Commons.driverMapCameraMoveF)driverLocationSwitchButton.setChecked(true);
        else driverLocationSwitchButton.setChecked(false);

        backButton = (ImageView)view.findViewById(R.id.btn_back);
        titleBox = (TextView)view.findViewById(R.id.titleBox);
        label1 = (TextView)view.findViewById(R.id.lb1);
        label2 = (TextView)view.findViewById(R.id.lb2);
        label3 = (TextView)view.findViewById(R.id.lb3);
        label4 = (TextView)view.findViewById(R.id.lb4);

        label5 = (TextView)view.findViewById(R.id.lb5);
        label6 = (TextView)view.findViewById(R.id.lb6);
        label7 = (TextView)view.findViewById(R.id.lb7);

        bold = Typeface.createFromAsset(getActivity().getAssets(), "futura medium bt.ttf");
        normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        notiSwitchButton = (Switch)view.findViewById(R.id.notification);
        notiSwitchButton.setTypeface(normal);
        notiSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //
                }else {
                    //
                }
            }
        });

        manageButton = (TextView)view.findViewById(R.id.btn_manage);
        manageButton.setTypeface(bold);
        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPayment();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        manageButton.setTypeface(bold);
        titleBox.setTypeface(bold);
        label1.setTypeface(bold);
        label2.setTypeface(bold);
        label3.setTypeface(normal);
        label4.setTypeface(normal);
        label5.setTypeface(normal);
        label6.setTypeface(normal);
        label7.setTypeface(normal);

        myLocationSwitchButton.setTypeface(normal);
        myLocationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.mapCameraMoveF = true;
                    if(Commons.driverMapCameraMoveF) {
                        Commons.driverMapCameraMoveF = false;
                        driverLocationSwitchButton.setChecked(false);
                    }
                }else {
                    Commons.mapCameraMoveF = false;
                }
            }
        });

        driverLocationSwitchButton.setTypeface(normal);
        driverLocationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.driverMapCameraMoveF = true;
                    if(Commons.mapCameraMoveF) {
                        Commons.mapCameraMoveF = false;
                        myLocationSwitchButton.setChecked(false);
                    }
                }else {
                    Commons.driverMapCameraMoveF = false;
                }
            }
        });

        mapViewSwitchButton.setTypeface(normal);
        mapViewSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.curMapTypeIndex = 2;
                }else {
                    Commons.curMapTypeIndex = 1;
                }
                Commons.googleMap.setMapType(LocationTrackingActivity.MAP_TYPES[Commons.curMapTypeIndex]);
            }
        });

        return view;
    }

    private void createNewPayment(){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "newpayment")
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
                                Commons.thisUser.set_acc_status(response.getString("status"));
                                Intent intent = new Intent(getActivity(), VerifyPaymentActivity.class);
                                intent.putExtra("acc_id", response.getString("acc_id"));
                                intent.putExtra("status", response.getString("status"));
                                startActivity(intent);
                            }else{
                                showToast("Server issue");
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

    public void showToast(String content){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.toast_view, null);
                TextView textView=(TextView)dialogView.findViewById(R.id.text);
                textView.setText(content);
                Toast toast=new Toast(getActivity());
                toast.setView(dialogView);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPaymentStatus();
    }

    private void getPaymentStatus(){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getpaymentstatus")
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
                                Commons.thisUser.set_acc_status(response.getString("status"));
                                if(response.getString("status").equals("completed")){
                                    label3.setText("Your payment has been verified");
                                    manageButton.setVisibility(View.GONE);
                                }
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


































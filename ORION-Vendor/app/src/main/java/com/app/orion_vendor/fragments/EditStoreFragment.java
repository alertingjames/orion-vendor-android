package com.app.orion_vendor.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.orion_vendor.R;

import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.main.LoadLogoActivity;
import com.app.orion_vendor.main.PickLocationActivity;
import com.app.orion_vendor.models.OrionAddress;
import com.app.orion_vendor.models.Store;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.app.orion_vendor.base.BaseActivity.isValidCellPhone;

public class EditStoreFragment extends Fragment {

    RoundedImageView logoBox;
    EditText nameBox, phoneBox, addressBox, delpriceBox, deldaysBox;
    TextView submitButton, label, label1;
    ImageButton mapButton;
    ImageView backButton;
    FrameLayout progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_edit_store, viewGroup, false);

        Commons.files.clear();
        Commons.imageFile = null;

        OrionAddress orionAddress = new OrionAddress();
        orionAddress.setLatLng(Commons.store.getLatLng());
        Commons.selectedOrionAddress = orionAddress;

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        logoBox = (RoundedImageView)view.findViewById(R.id.logoBox);
        nameBox = (EditText)view.findViewById(R.id.nameBox);
        phoneBox = (EditText)view.findViewById(R.id.phoneBox);
        addressBox = (EditText)view.findViewById(R.id.addressBox);
        submitButton = (TextView)view.findViewById(R.id.btn_submit);
        mapButton = (ImageButton) view.findViewById(R.id.btn_map);
        backButton = (ImageView)view.findViewById(R.id.btn_back);

        label = (TextView)view.findViewById(R.id.lb);
        label1 = (TextView)view.findViewById(R.id.lb1);

        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        label.setTypeface(normal);
        label1.setTypeface(normal);
        nameBox.setTypeface(normal);
        phoneBox.setTypeface(normal);
        addressBox.setTypeface(normal);
        addressBox.setEnabled(false);
        submitButton.setTypeface(normal);

        delpriceBox = (EditText)view.findViewById(R.id.deliveryPriceBox);
        delpriceBox.setTypeface(normal);

        deldaysBox = (EditText)view.findViewById(R.id.deliveryDaysBox);
        deldaysBox.setTypeface(normal);

        logoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.logoBox = logoBox;
                Intent intent = new Intent(getActivity(), LoadLogoActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0,0);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.textView = addressBox;
                Intent intent = new Intent(getActivity(), PickLocationActivity.class);
                startActivity(intent);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editStore();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        Glide.with(getActivity()).load(Commons.store.getLogoUrl()).into(logoBox);
        nameBox.setText(Commons.store.getName());
        phoneBox.setText(Commons.store.getPhoneNumber());
        addressBox.setText(Commons.store.getAddress());
        delpriceBox.setText(String.valueOf(Commons.store.getDelivery_price()));
        deldaysBox.setText(String.valueOf(Commons.store.getDelivery_days()));

        setupUI(view, getActivity());

        return view;
    }

    public void setupUI(View view, Activity activity) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    try{
                        hideSoftKeyboard(activity);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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

    private void editStore(){

        if(nameBox.getText().length() == 0){
            showToast("Enter store name.");
            return;
        }

        if(phoneBox.getText().length() == 0){
            showToast("Enter store phone number.");
            return;
        }

        if(!isValidCellPhone(phoneBox.getText().toString().trim())){
            showToast("The phone number is invalid. Please enter a valid phone number.");
            return;
        }

        if(addressBox.getText().length() == 0){
            showToast("Enter store address.");
            return;
        }

        if(delpriceBox.getText().length() == 0){
            showToast("Enter product delivery price.");
            return;
        }

        if(deldaysBox.getText().length() == 0){
            showToast("Enter product delivery days.");
            return;
        }

        editStore(Commons.files, nameBox.getText().toString().trim(), phoneBox.getText().toString(), addressBox.getText().toString().trim(), Commons.selectedOrionAddress);

    }

    private void editStore(ArrayList<File> files, String name, String phone, String address, OrionAddress orionAddress) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "editStore")
                .addMultipartFileList("files", files)
                .addPathParameter("store_id", String.valueOf(Commons.store.getId()))
                .addMultipartParameter("name", name)
                .addMultipartParameter("phone_number", phone)
                .addMultipartParameter("address", address)
                .addMultipartParameter("delivery_price", delpriceBox.getText().toString())
                .addMultipartParameter("delivery_days", deldaysBox.getText().toString())
                .addMultipartParameter("latitude", String.valueOf(orionAddress.getLatLng().latitude))
                .addMultipartParameter("longitude", String.valueOf(orionAddress.getLatLng().longitude))
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("UPLOADED!!!", String.valueOf(bytesUploaded) + "/" + String.valueOf(totalBytes));
                    }
                })
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
                                Store store = new Store();
                                store.setId(object.getInt("id"));
                                store.setName(object.getString("name"));
                                store.setPhoneNumber(object.getString("phone_number"));
                                store.setAddress(object.getString("address"));
                                store.setDelivery_price(Double.parseDouble(object.getString("delivery_price")));
                                store.setDelivery_days(object.getInt("delivery_days"));
                                store.setLogoUrl(object.getString("logo_url"));
                                store.set_registered_time(object.getString("registered_time"));
                                store.set_status(object.getString("status"));
                                double lat = 0.0d, lng = 0.0d;
                                if(object.getString("latitude").length() > 0){
                                    lat = Double.parseDouble(object.getString("latitude"));
                                    lng = Double.parseDouble(object.getString("longitude"));
                                }
                                store.setLatLng(new LatLng(lat, lng));

                                Commons.store = store;
                                showToast("Successfully updated.");

                                Commons.thisUser.set_stores(1);

                                getActivity().onBackPressed();

                            }else if(result.equals("1")){
                                showToast("Your store doesn't exist.");
                                Commons.thisUser.set_stores(0);
                                toInit();
                            }else {
                                showToast("Server issue.");
//                                toInit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            toInit();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
//                        toInit();
                    }
                });
    }

    private void toInit(){
        Fragment fragment = new RegisterStoreFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.output, fragment);
        transaction.commit();
    }

}





































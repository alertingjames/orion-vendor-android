package com.app.orion_vendor.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyPaymentActivity extends BaseActivity {

    ImageView backButton;
    TextView titleBox, country, accidBox;
    TextView submitButton;
    EditText bank, routing, city, address, postal, state, last4, bdate, bmonth, byear;
    FrameLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_payment);

        progressBar = (FrameLayout) findViewById(R.id.loading_bar);

        backButton = (ImageView)findViewById(R.id.btn_back);
        titleBox = (TextView)findViewById(R.id.titleBox);
        submitButton = (TextView)findViewById(R.id.btn_submit);
        accidBox = (TextView)findViewById(R.id.accidBox);

        String accId = getIntent().getStringExtra("acc_id");
        String status = getIntent().getStringExtra("status");
        accidBox.setTypeface(bold);
        accidBox.setText("Stripe: " + accId + ": " + status);
        if(status.equals("pending"))titleBox.setText("Register Payment Info");
        else if (status.equals("completed"))titleBox.setText("Updated Payment Info");

        bank = (EditText)findViewById(R.id.bankNumber);
        routing = (EditText)findViewById(R.id.routing);
        country = (TextView)findViewById(R.id.countryContainer);
        address = (EditText)findViewById(R.id.addressLine);
        city = (EditText)findViewById(R.id.city);
        postal = (EditText)findViewById(R.id.postalCode);
        state = (EditText)findViewById(R.id.state);
        last4 = (EditText)findViewById(R.id.ssnLastFour);
        bdate = (EditText)findViewById(R.id.day);
        bmonth = (EditText)findViewById(R.id.month);
        byear = (EditText)findViewById(R.id.year);

        titleBox.setTypeface(bold);
        bank.setTypeface(normal);
        routing.setTypeface(normal);
        country.setTypeface(normal);
        city.setTypeface(normal);
        postal.setTypeface(normal);
        state.setTypeface(normal);
        last4.setTypeface(normal);
        bdate.setTypeface(normal);
        bmonth.setTypeface(normal);
        byear.setTypeface(normal);
        submitButton.setTypeface(bold);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setupUI(findViewById(R.id.activity), this);
    }

    public void submitPaymentInfo(View view){
        if(bank.getText().toString().trim().length() == 0){
            showToast("Enter your bank number.");
            return;
        }
        if(routing.getText().toString().trim().length() == 0){
            showToast("Enter your routing number.");
            return;
        }
        if(country.getText().toString().trim().length() == 0){
            showToast("Enter your country name.");
            return;
        }
        if(city.getText().toString().trim().length() == 0){
            showToast("Enter your city name.");
            return;
        }
        if(address.getText().toString().trim().length() == 0){
            showToast("Enter your address.");
            return;
        }
        if(postal.getText().toString().trim().length() == 0){
            showToast("Enter your postal code.");
            return;
        }
        if(state.getText().toString().trim().length() == 0){
            showToast("Enter your state name.");
            return;
        }
        if(last4.getText().toString().trim().length() == 0){
            showToast("Enter SSN last 4.");
            return;
        }
        if(bdate.getText().toString().trim().length() == 0){
            showToast("Enter your birthday date.");
            return;
        }
        if(bmonth.getText().toString().trim().length() == 0){
            showToast("Enter your birthday month.");
            return;
        }
        if(byear.getText().toString().trim().length() == 0){
            showToast("Enter your birthday year.");
            return;
        }

        uploadPaymentInfo();
    }

    private void uploadPaymentInfo(){

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "completepayment")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("bank_number", bank.getText().toString().trim())
                .addBodyParameter("routing_number", routing.getText().toString().trim())
                .addBodyParameter("country", country.getText().toString().trim())
                .addBodyParameter("city", city.getText().toString().trim())
                .addBodyParameter("address", address.getText().toString().trim())
                .addBodyParameter("postal_code", postal.getText().toString().trim())
                .addBodyParameter("state", state.getText().toString().trim())
                .addBodyParameter("ssn_last4", last4.getText().toString().trim())
                .addBodyParameter("day", bdate.getText().toString())
                .addBodyParameter("month", bmonth.getText().toString())
                .addBodyParameter("year", byear.getText().toString())
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
                                showToast("Your payment has been verified.");
                                Commons.thisUser.set_acc_status("completed");
                                onBackPressed();
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

}













































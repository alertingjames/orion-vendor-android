package com.app.orion_vendor.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.DriverListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.User;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class DriverListActivity extends BaseActivity {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;
    ListView list;
    FrameLayout progressBar;

    ArrayList<User> drivers = new ArrayList<>();
    DriverListAdapter adapter = new DriverListAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = (FrameLayout) findViewById(R.id.loading_bar);
        title = (TextView)findViewById(R.id.title);

        searchBar = (LinearLayout)findViewById(R.id.search_bar);
        searchButton = (ImageView)findViewById(R.id.searchButton);
        cancelButton = (ImageView)findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();

        title.setTypeface(bold);
        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().trim().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        list = (ListView) findViewById(R.id.list);

        setupUI((FrameLayout)findViewById(R.id.activity), this);

    }

    public void search(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDrivers();
    }

    private void getDrivers() {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getDrivers")
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
                                drivers.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
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

                                    Location myLocation = new Location("MyLocation");
                                    myLocation.setLatitude(Commons.thisUser.getLatLng().latitude);
                                    myLocation.setLongitude(Commons.thisUser.getLatLng().longitude);
                                    Location driverLocation = new Location("DealLocation");
                                    driverLocation.setLatitude(user.getLatLng().latitude);
                                    driverLocation.setLongitude(user.getLatLng().longitude);
                                    double distance = myLocation.distanceTo(driverLocation);

                                    user.setDistance(distance);

                                    drivers.add(user);
                                }

                                if(drivers.isEmpty())((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);

                                Collections.sort(drivers);

                                adapter.setDatas(drivers);
                                list.setAdapter(adapter);

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



































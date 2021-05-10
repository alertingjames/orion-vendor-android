package com.app.orion_vendor.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.BrandListAdapter;
import com.app.orion_vendor.classes.CustomGridView;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.Brand;
import com.app.orion_vendor.models.Store;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyStoreFragment extends Fragment {

    RoundedImageView logoBox;
    TextView label, label1, label2;
    TextView nameBox, phoneBox, addressBox;
    TextView editStoreButton, addBrandButton;
    CustomGridView listBox;
    RatingBar ratingBar;
    TextView ratingsBox, reviewsBox;
    FrameLayout noResultLayout;
    FrameLayout progressBar;
    ArrayList<Brand> brands = new ArrayList<>();
    BrandListAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_store, viewGroup, false);

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        logoBox = (RoundedImageView)view.findViewById(R.id.logoBox);

        label = (TextView) view.findViewById(R.id.lb);
        label1 = (TextView) view.findViewById(R.id.lb1);
        label2 = (TextView) view.findViewById(R.id.lb2);

        nameBox = (TextView) view.findViewById(R.id.nameBox);
        phoneBox = (TextView) view.findViewById(R.id.phoneBox);
        addressBox = (TextView) view.findViewById(R.id.addressBox);
        editStoreButton = (TextView) view.findViewById(R.id.btn_edit_store);
        addBrandButton = (TextView) view.findViewById(R.id.btn_add_brand);

        ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
        ratingsBox = (TextView) view.findViewById(R.id.ratings);

        reviewsBox = (TextView) view.findViewById(R.id.reviewsBox);

        listBox = (CustomGridView) view.findViewById(R.id.list);
        noResultLayout = (FrameLayout) view.findViewById(R.id.no_result);

        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        label.setTypeface(normal);
        label1.setTypeface(normal);
        label2.setTypeface(normal);
        nameBox.setTypeface(normal);
        phoneBox.setTypeface(normal);
        addressBox.setTypeface(normal);
        editStoreButton.setTypeface(normal);
        addBrandButton.setTypeface(normal);

        editStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new EditStoreFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.addToBackStack(null).commit();
            }
        });

        addBrandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new NewBrandFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.addToBackStack(null).commit();
            }
        });

        return view;
    }

    private void getMyStore(String memberId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getMyStore")
                .addBodyParameter("member_id", memberId)
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
                                Store store = new Store();
                                store.setId(object.getInt("id"));
                                store.setUserId(object.getInt("member_id"));
                                store.setName(object.getString("name"));
                                store.setPhoneNumber(object.getString("phone_number"));
                                store.setAddress(object.getString("address"));
                                store.setDelivery_price(Double.parseDouble(object.getString("delivery_price")));
                                store.setDelivery_days(object.getInt("delivery_days"));
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

                                Commons.store = store;

                                Glide.with(getActivity()).load(Commons.store.getLogoUrl()).into(logoBox);
                                nameBox.setText(Commons.store.getName());
                                phoneBox.setText(Commons.store.getPhoneNumber());
                                addressBox.setText(Commons.store.getAddress());
                                ratingBar.setRating(Commons.store.getRatings());
                                ratingsBox.setText(String.valueOf(Commons.store.getRatings()));
                                reviewsBox.setText(String.valueOf(Commons.store.getReviews()));

                                getStoreBrands(String.valueOf(Commons.thisUser.get_idx()), String.valueOf(Commons.store.getId()));

                            }else if(result.equals("1")){
                                showToast("You haven't registered any store yet.");
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

    private void toInit(){
        Fragment fragment = new RegisterStoreFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.output, fragment);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyStore(String.valueOf(Commons.thisUser.get_idx()));
    }

    private void getStoreBrands(String memberId, String storeId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getStoreBrands")
                .addBodyParameter("member_id", memberId)
                .addBodyParameter("store_id", storeId)
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
                                brands.clear();
                                JSONArray jsonArray = response.getJSONArray("data");
                                for(int i=0;i<jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Brand brand = new Brand();
                                    brand.setId(object.getInt("id"));
                                    brand.setUserId(object.getInt("member_id"));
                                    brand.setStoreId(object.getInt("store_id"));
                                    brand.setName(object.getString("name"));
                                    brand.setCategory(object.getString("category"));
                                    brand.setLogoUrl(object.getString("logo_url"));
                                    brand.setRegisteredTime(object.getString("registered_time"));
                                    brand.setStatus(object.getString("status"));

                                    brands.add(brand);
                                }

                                if(brands.isEmpty())noResultLayout.setVisibility(View.VISIBLE);
                                else noResultLayout.setVisibility(View.GONE);
                                if(getActivity() != null){
                                    adapter = new BrandListAdapter(getActivity());
                                    adapter.setDatas(brands);
                                    listBox.setAdapter(adapter);
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
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });
    }

}































package com.app.orion_vendor.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.app.orion_vendor.adapters.MyBrandProductListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.main.NewProductActivity;
import com.app.orion_vendor.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class MyBrandProductsFragment extends Fragment {

    GridView listBox;
    FrameLayout noResultLayout;
    FrameLayout progressBar;
    ImageView searchButton, cancelButton, addProductButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    ArrayList<Product> products = new ArrayList<>();
    MyBrandProductListAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_brand_products, viewGroup, false);

        Commons.myBrandProductsFragment = this;

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        searchBar = (LinearLayout)view.findViewById(R.id.search_bar);
        searchButton = (ImageView)view.findViewById(R.id.searchButton);
        cancelButton = (ImageView)view.findViewById(R.id.cancelButton);
        addProductButton = (ImageView)view.findViewById(R.id.btn_add_product);

        ui_edtsearch = (EditText)view.findViewById(R.id.edt_search);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();

        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");
        ui_edtsearch.setTypeface(normal);

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

        listBox = (GridView) view.findViewById(R.id.list);
        noResultLayout = (FrameLayout) view.findViewById(R.id.no_result);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                cancelButton.setVisibility(View.VISIBLE);
//                searchButton.setVisibility(View.GONE);
//                searchBar.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                cancelButton.setVisibility(View.GONE);
//                searchButton.setVisibility(View.VISIBLE);
//                searchBar.setVisibility(View.GONE);
                ui_edtsearch.setText("");
            }
        });

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Commons.thisUser.get_acc_status().equals("completed")){
                    showToast("Please complete payment verification.");
                    Fragment fragment = new VendorSettingsFragment();
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.activity, fragment);
                    transaction.addToBackStack(null).commit();
                    return;
                }

                if(Commons.thisUser.get_phone_number().length() == 0){
                    showToast("Please complete your profile.");
                    Fragment fragment = new ProfileFragment();
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.activity, fragment);
                    transaction.addToBackStack(null).commit();
                    return;
                }

                Intent intent = new Intent(getActivity(), NewProductActivity.class);
                startActivity(intent);

            }
        });

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

    public static MyBrandProductsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("Brand Details", page);
        MyBrandProductsFragment fragment = new MyBrandProductsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int mPage = getArguments().getInt("Brand Details");
        Log.d("Pager NO", String.valueOf(mPage));
    }

    private void getBrandProducts(String brandId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getBrandProducts")
                .addBodyParameter("brand_id", brandId)
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
                                products.clear();
                                JSONArray jsonArray = response.getJSONArray("data");
                                for(int i=0;i<jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
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
                                    product.setDelivery_price(Double.parseDouble(object.getString("delivery_price")));
                                    product.setDelivery_days(Integer.parseInt(object.getString("delivery_days")));

                                    product.setLikes(object.getInt("likes"));
                                    product.setRatings(Float.parseFloat(object.getString("ratings")));

                                    products.add(product);
                                }

                                if(products.isEmpty())noResultLayout.setVisibility(View.VISIBLE);
                                else noResultLayout.setVisibility(View.GONE);
                                if(getActivity() != null){
                                    adapter = new MyBrandProductListAdapter(getActivity());
                                    adapter.setDatas(products);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BRAND ID!!!", String.valueOf(Commons.brand.getId()));
        getBrandProducts(String.valueOf(Commons.brand.getId()));
    }

    public void delProduct(String productId){

        ((BaseActivity) getActivity()).showAlertDialogForQuestion("Warning!", "Are you sure you want to delete this product?", getActivity(), null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                progressBar.setVisibility(View.VISIBLE);
                AndroidNetworking.post(ReqConst.SERVER_URL + "deleteProduct")
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
                                        showToast("Product deleted.");
                                        onResume();
                                    }else if(result.equals("1")){
                                        showToast("The product doesn't exist.");
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
                return null;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Commons.myBrandProductsFragment = null;
    }
}































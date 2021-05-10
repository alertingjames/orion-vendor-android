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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.CategoryItemListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.classes.ProductOptions;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.main.LoadLogoActivity;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class EditBrandFragment extends Fragment {

    RoundedImageView logoBox;
    EditText nameBox;
    TextView categoryBox;
    TextView submitButton, label, label1;
    ImageButton categoryButton;
    ImageView cancelButton, deleteButton;
    ListView listView;
    LinearLayout categoryLayout;
    FrameLayout progressBar;

    ArrayList<String> categoryList = new ArrayList<>();
    CategoryItemListAdapter adapter = null;
    private int mPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_edit_brand, viewGroup, false);

        Commons.files.clear();
        Commons.imageFile = null;

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        logoBox = (RoundedImageView)view.findViewById(R.id.logoBox);
        nameBox = (EditText)view.findViewById(R.id.nameBox);
        categoryBox = (TextView) view.findViewById(R.id.categoryBox);
        submitButton = (TextView)view.findViewById(R.id.btn_submit);
        categoryButton = (ImageButton) view.findViewById(R.id.btn_category);
        cancelButton = (ImageView)view.findViewById(R.id.btn_category_cancel);
        deleteButton = (ImageView)view.findViewById(R.id.btn_trash);
        categoryLayout = (LinearLayout) view.findViewById(R.id.categoryLayout);

        listView = (ListView)view.findViewById(R.id.list);

        label = (TextView)view.findViewById(R.id.lb);
        label1= (TextView)view.findViewById(R.id.lb1);

        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        nameBox.setTypeface(normal);
        categoryBox.setTypeface(normal);
        submitButton.setTypeface(normal);

        label.setTypeface(normal);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "futura medium bt.ttf");
        label1.setTypeface(bold);

        categoryList.clear();
        categoryList.addAll(Arrays.asList(ProductOptions.categoryItems));
        if(getActivity() != null){
            adapter = new CategoryItemListAdapter(getActivity());
            adapter.setDatas(categoryList);
            listView.setAdapter(adapter);
        }


        Animation animation1 = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_in);
        Animation animation2 = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_out);

        logoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.logoBox = logoBox;
                Intent intent = new Intent(getActivity(), LoadLogoActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0,0);
            }
        });

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.textView = categoryBox;
                Commons.layout = categoryLayout;
                categoryLayout.setAnimation(animation1);
                categoryLayout.setVisibility(View.VISIBLE);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editBrand();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryLayout.setAnimation(animation2);
                categoryLayout.setVisibility(View.GONE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).showAlertDialogForQuestion("Warning!", "Are you sure you want to delete this brand?", getActivity(), null, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        progressBar.setVisibility(View.VISIBLE);
                        AndroidNetworking.post(ReqConst.SERVER_URL + "deleteBrand")
                                .addBodyParameter("brand_id", String.valueOf(Commons.brand.getId()))
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
                                                showToast("Brand deleted.");
                                                getActivity().onBackPressed();
                                            }else if(result.equals("1")){
                                                showToast("The brand doesn't exist.");
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
        });

        Glide.with(getActivity()).load(Commons.brand.getLogoUrl()).into(logoBox);
        nameBox.setText(Commons.brand.getName());
        categoryBox.setText(Commons.brand.getCategory());

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

    public static EditBrandFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("Brand Details", page);
        EditBrandFragment fragment = new EditBrandFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt("Brand Details");
        Log.d("Pager NO", String.valueOf(mPage));
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

    private void editBrand(){

        if(nameBox.getText().length() == 0){
            showToast("Enter brand name.");
            return;
        }

        if(categoryBox.getText().length() == 0){
            showToast("Choose a category.");
            return;
        }

        addNewStore(Commons.files, nameBox.getText().toString().trim(), categoryBox.getText().toString().trim());

    }

    private void addNewStore(ArrayList<File> files, String name, String category) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "editBrand")
                .addMultipartFileList("files", files)
                .addPathParameter("brand_id", String.valueOf(Commons.brand.getId()))
                .addMultipartParameter("name", name)
                .addMultipartParameter("category", category)
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
                                showToast("The brand has been updated.");
                            }else if(result.equals("1")){
                                showToast("The brand doesn't exist.");
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































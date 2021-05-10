package com.app.orion_vendor.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.CategoryItemListAdapter;
import com.app.orion_vendor.classes.ProductOptions;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.Constants;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.main.LoadLogoActivity;
import com.app.orion_vendor.main.PickLocationActivity;
import com.app.orion_vendor.models.Store;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class NewBrandFragment extends Fragment {

    RoundedImageView logoBox;
    EditText nameBox;
    TextView categoryBox;
    TextView submitButton, label, label1;
    ImageButton categoryButton;
    ImageView backButton, cancelButton;
    ListView listView;
    LinearLayout categoryLayout;
    FrameLayout progressBar;

    ArrayList<String> categoryList = new ArrayList<>();
    CategoryItemListAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_new_brand, viewGroup, false);

        Commons.imageFile = null;

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);

        logoBox = (RoundedImageView)view.findViewById(R.id.logoBox);
        nameBox = (EditText)view.findViewById(R.id.nameBox);
        categoryBox = (TextView) view.findViewById(R.id.categoryBox);
        submitButton = (TextView)view.findViewById(R.id.btn_submit);
        categoryButton = (ImageButton) view.findViewById(R.id.btn_category);
        backButton = (ImageView)view.findViewById(R.id.btn_back);
        cancelButton = (ImageView)view.findViewById(R.id.btn_category_cancel);
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
                addNewBrand();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryLayout.setAnimation(animation2);
                categoryLayout.setVisibility(View.GONE);
            }
        });

        return view;
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

    private void addNewBrand(){

        if(nameBox.getText().length() == 0){
            showToast("Enter brand name.");
            return;
        }

        if(categoryBox.getText().length() == 0){
            showToast("Choose a category.");
            return;
        }

        if(Commons.imageFile == null){
            showToast("Load brand logo image.");
            return;
        }

        addNewStore(Commons.imageFile, nameBox.getText().toString().trim(), categoryBox.getText().toString().trim());

    }

    private void addNewStore(File file, String name, String category) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "addBrand")
                .addMultipartFile("file", file)
                .addPathParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addPathParameter("store_id", String.valueOf(Commons.store.getId()))
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
                                showToast("The brand has been added.");
                                getActivity().onBackPressed();
                            }else if(result.equals("1")){
                                showToast("The same brand already exists. Try another one.");
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



































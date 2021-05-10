package com.app.orion_vendor.main;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.CategoryItemListAdapter;
import com.app.orion_vendor.adapters.PictureListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.classes.ProductOptions;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.Constants;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.Picture;
import com.app.orion_vendor.models.Product;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.material.appbar.AppBarLayout;
import com.marozzi.roundbutton.RoundButton;
import com.rd.PageIndicatorView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProductActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private View mFab;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    Toolbar toolbar;
    ImageView pictureBox;
    ImageButton cancelButton;
    TextView submitButton;
    ImageButton subcategoryButton, genderButton;
    LinearLayout categoryLayout;
    ImageView categoryCancelButton;
    ListView listView;
    EditText nameBox, priceBox, descBox, delpriceBox, deldaysBox;
    TextView categoryBox, subcategoryBox, genderBox, storeNameBox, brandNameBox;
    CircleImageView brandLogoBox;
    File imageFile = null;
    LinearLayout loadingLayout;
    TextView progressText;
    TextView label, label1;
    FrameLayout progressBar;
    ArrayList<File> fileList = new ArrayList<>();

    ViewPager pager;
    PageIndicatorView pageIndicatorView;
    ArrayList<Picture> pictures = new ArrayList<>();
    PictureListAdapter adapter = new PictureListAdapter(this);

    ArrayList<String> categoryList = new ArrayList<>();
    CategoryItemListAdapter categoryItemListAdapter = new CategoryItemListAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        mFab = findViewById(R.id.flexible_example_fab);

        toolbar = (Toolbar) findViewById(R.id.flexible_example_toolbar);
        toolbar.setTitle("Masked Dates");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.flexible_example_appbar);
        appbar.addOnOffsetChangedListener(this);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditProductActivity.this);
            }
        });

        progressBar = (FrameLayout) findViewById(R.id.loading_bar);

        pager = findViewById(R.id.viewPager);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        nameBox = (EditText)findViewById(R.id.nameBox);
        nameBox.setTypeface(normal);

        categoryBox = (TextView)findViewById(R.id.categoryBox);
        categoryBox.setTypeface(normal);

        subcategoryBox = (TextView)findViewById(R.id.subcategoryBox);
        subcategoryBox.setTypeface(normal);

        genderBox = (TextView)findViewById(R.id.genderBox);
        genderBox.setTypeface(normal);

        priceBox = (EditText)findViewById(R.id.priceBox);
        priceBox.setTypeface(normal);

        descBox = (EditText)findViewById(R.id.descriptionBox);
        descBox.setTypeface(normal);

        label = (TextView)findViewById(R.id.lb);
        label.setTypeface(bold);

        label1 = (TextView)findViewById(R.id.lb1);
        label1.setTypeface(bold);

        brandLogoBox = (CircleImageView)findViewById(R.id.brandLogoBox);
        Glide.with(this).load(Commons.brand.getLogoUrl()).into(brandLogoBox);

        pictureBox = (ImageView)findViewById(R.id.pictureBox);
        cancelButton = (ImageButton)findViewById(R.id.btn_cancel);
        subcategoryButton = (ImageButton) findViewById(R.id.btn_subcategory);
        genderButton = (ImageButton) findViewById(R.id.btn_gender);
        submitButton = (TextView)findViewById(R.id.btn_submit);

        submitButton.setTypeface(normal);

        categoryCancelButton = (ImageView)findViewById(R.id.btn_category_cancel);
        categoryLayout = (LinearLayout) findViewById(R.id.categoryLayout);

        listView = (ListView)findViewById(R.id.list);

        loadingLayout = (LinearLayout)findViewById(R.id.progressLayout);
        progressText = (TextView)findViewById(R.id.progressText);

        delpriceBox = (EditText)findViewById(R.id.deliveryPriceBox);
        delpriceBox.setTypeface(normal);
        delpriceBox.setText(String.valueOf(Commons.product.getDelivery_price()));

        deldaysBox = (EditText)findViewById(R.id.deliveryDaysBox);
        deldaysBox.setTypeface(normal);
        deldaysBox.setText(String.valueOf(Commons.product.getDelivery_days()));

        storeNameBox = (TextView) findViewById(R.id.storeNameBox);
        storeNameBox.setTypeface(normal);

        brandNameBox = (TextView) findViewById(R.id.brandNameBox);
        brandNameBox.setTypeface(normal);

        storeNameBox.setText(Commons.store.getName());
        brandNameBox.setText(Commons.brand.getName());

        Animation animation1 = AnimationUtils.loadAnimation(this,
                R.anim.bottom_in);
        Animation animation2 = AnimationUtils.loadAnimation(this,
                R.anim.bottom_out);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitProduct();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delCurrentItem();
            }
        });

        subcategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.textView = subcategoryBox;
                Commons.layout = categoryLayout;
                categoryLayout.setAnimation(animation1);
                categoryLayout.setVisibility(View.VISIBLE);

                categoryList.clear();
                categoryList.addAll(Arrays.asList(ProductOptions.subcategoryItems));
                categoryItemListAdapter.setDatas(categoryList);
                listView.setAdapter(categoryItemListAdapter);
            }
        });

        genderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.textView = genderBox;
                Commons.layout = categoryLayout;
                categoryLayout.setAnimation(animation1);
                categoryLayout.setVisibility(View.VISIBLE);

                categoryList.clear();
                categoryList.addAll(Arrays.asList(ProductOptions.genderItems));
                categoryItemListAdapter.setDatas(categoryList);
                listView.setAdapter(categoryItemListAdapter);
            }
        });

        categoryCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryLayout.setAnimation(animation2);
                categoryLayout.setVisibility(View.GONE);
            }
        });

        nameBox.setText(Commons.product.getName());
        categoryBox.setText(Commons.brand.getCategory());
        subcategoryBox.setText(Commons.product.getSubcategory());
        genderBox.setText(Commons.product.getGender());
        priceBox.setText(String.valueOf(Commons.product.getNew_price() > 0?Commons.product.getNew_price():Commons.product.getPrice()));
        descBox.setText(Commons.product.getDescription());

        if(Commons.product.get_pictureList().size() > 0){
            pictureBox.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
        }
        pictures.clear();
        pictures.addAll(Commons.product.get_pictureList());
        adapter.setDatas(pictures);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        setupUI(findViewById(R.id.activity), this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100
                / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                ViewCompat.animate(mFab).scaleY(0).scaleX(0).start();

                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view)).setVisibility(View.VISIBLE);
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .start();

            }
        }

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE + 70) {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
            toolbar.setAnimation(animation);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }else if (currentScrollPercentage <= PERCENTAGE_TO_SHOW_IMAGE + 70) {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
            toolbar.setAnimation(animation);
            toolbar.setBackground(null);

        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(mFab).scaleY(1).scaleX(1).start();

                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .start();
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view)).setVisibility(View.GONE);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //From here you can load the image however you need to, I recommend using the Glide library
                imageFile = new File(resultUri.getPath());

                File imageFile = new File(resultUri.getPath());
                Picture picture = new Picture();
                picture.setFile(imageFile);
                picture.setUrl("");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    picture.setBitmap(bitmap);
                    picture.setIdx(pictures.size());
                    pictures.add(picture);
                    adapter.setDatas(pictures);
                    adapter.notifyDataSetChanged();
                    pager.setAdapter(adapter);
                    pager.setCurrentItem(pictures.size());
                    if(pictureBox.getVisibility() == View.VISIBLE)pictureBox.setVisibility(View.GONE);
                    if(cancelButton.getVisibility() == View.GONE)cancelButton.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void delCurrentItem(){
        if(pictures.size() > 0){
            if(pictures.get(pager.getCurrentItem()).getUrl().length() > 0){
                showAlertDialogForQuestion("Warning!", "Are you sure you want to delete this picture?", this, null, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        progressBar.setVisibility(View.VISIBLE);
                        AndroidNetworking.post(ReqConst.SERVER_URL + "delProductPicture")
                                .addBodyParameter("product_id", String.valueOf(Commons.product.getIdx()))
                                .addBodyParameter("picture_url", pictures.get(pager.getCurrentItem()).getUrl())
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
                                                showToast("Deleted.");
                                                pictures.remove(pager.getCurrentItem());
                                                ArrayList<Picture> pictures1 = new ArrayList<>();
                                                for(Picture picture:pictures){
                                                    if(picture.getUrl().length() > 0){
                                                        pictures1.add(picture);
                                                    }
                                                }
                                                Commons.product.set_pictureList(pictures1);
                                                if(pictures.size() == 0) {
                                                    if(pictureBox.getVisibility() == View.GONE)pictureBox.setVisibility(View.VISIBLE);
                                                    if(cancelButton.getVisibility() == View.VISIBLE)cancelButton.setVisibility(View.GONE);
                                                }
                                                adapter.setDatas(pictures);
                                                adapter.notifyDataSetChanged();
                                                pager.setAdapter(adapter);
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
            else {
                pictures.remove(pager.getCurrentItem());
                if(pictures.size() == 0) {
                    if(pictureBox.getVisibility() == View.GONE)pictureBox.setVisibility(View.VISIBLE);
                    if(cancelButton.getVisibility() == View.VISIBLE)cancelButton.setVisibility(View.GONE);
                }
                adapter.setDatas(pictures);
                adapter.notifyDataSetChanged();
                pager.setAdapter(adapter);
            }
        }

    }


    private void submitProduct(){

        if(nameBox.getText().length() == 0){
            showToast("Enter product name.");
            return;
        }

        if(subcategoryBox.getText().length() == 0){
            showToast("Enter product category.");
            return;
        }

        if(genderBox.getText().length() == 0){
            showToast("Enter gender.");
            return;
        }

        if(priceBox.getText().length() == 0){
            showToast("Enter product price.");
            return;
        }

        if(descBox.getText().length() == 0){
            showToast("Enter product description.");
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

        if(pictures.size() == 0){
            showToast("Load at least a product picture.");
            return;
        }

        fileList.clear();
        if(pictures.size() > 0){
            for(Picture picture:pictures){
                if (picture.getFile() != null)
                    fileList.add(picture.getFile());
            }
        }

        ProductOptions options = new ProductOptions();

        loadingLayout.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "editProduct")
                .addMultipartFileList("files", fileList)
                .addMultipartParameter("product_id", String.valueOf(Commons.product.getIdx()))
                .addMultipartParameter("name", nameBox.getText().toString().trim())
                .addMultipartParameter("subcategory", subcategoryBox.getText().toString())
                .addMultipartParameter("gender", genderBox.getText().toString())
                .addMultipartParameter("gender_key", options.getGenderKey(genderBox.getText().toString()))
                .addMultipartParameter("price", priceBox.getText().toString())
                .addMultipartParameter("description", descBox.getText().toString())
                .addMultipartParameter("delivery_price", delpriceBox.getText().toString())
                .addMultipartParameter("delivery_days", deldaysBox.getText().toString())
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        String uploaded = String.valueOf((int)bytesUploaded*100/totalBytes);
                        Log.d("UPLOADED!!!", uploaded);
                        progressText.setText(uploaded);

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        loadingLayout.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                JSONObject object = response.getJSONObject("data");
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

                                product.setLikes(object.getInt("likes"));
                                product.setRatings(Float.parseFloat(object.getString("ratings")));

                                Commons.product = product;

                                showToast("The product has been updated.");
                                finish();
                            }else if(result.equals("1")){
                                showToast("The product doesn't exist.");
                            }else {
                                showToast("Server issue...");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        loadingLayout.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });

    }

}































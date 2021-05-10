package com.app.orion_vendor.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.MyBrandProductListAdapter;
import com.app.orion_vendor.adapters.PictureListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.Picture;
import com.app.orion_vendor.models.Product;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.material.appbar.AppBarLayout;
import com.rd.PageIndicatorView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProductDetailActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    AVLoadingIndicatorView progressBar;
    Toolbar toolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden = false;

    TextView priceBox, oldPriceBox, brandNameBox, storeNameBox, nameBox, descriptionBox, likeCountBox;
    FrameLayout oldPriceFrame, likesLayout, likeCountFrame;
    CircleImageView brandLogoBox, storeLogoBox;
    private View mFab;

    RatingBar ratingBar;
    TextView ratingsBox, deliveryBox;

    ViewPager pager;
    PageIndicatorView pageIndicatorView;
    ArrayList<Picture> pictures = new ArrayList<>();
    PictureListAdapter adapter = new PictureListAdapter(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product_detail);

        mFab = findViewById(R.id.flexible_example_fab);

        toolbar = (Toolbar) findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.flexible_example_appbar);

   //     setTitle(Commons.product.getName());

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditProductActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        ratingBar = (RatingBar)findViewById(R.id.ratingbar);
        ratingsBox = (TextView)findViewById(R.id.ratings);
        ratingBar.setRating(Commons.product.getRatings());
        ratingsBox.setText(String.valueOf(Commons.product.getRatings()));

        deliveryBox = (TextView)findViewById(R.id.deliveryBox);
        deliveryBox.setTypeface(normal);
        deliveryBox.setText(String.valueOf(Commons.product.getDelivery_days()) + " Days, " + String.valueOf(Commons.product.getDelivery_price()) + " SGD");

        priceBox = (TextView)findViewById(R.id.priceBox);
        oldPriceBox = (TextView)findViewById(R.id.oldPriceBox);
        brandNameBox = (TextView)findViewById(R.id.brandNameBox);
        storeNameBox = (TextView)findViewById(R.id.storeNameBox);
        nameBox = (TextView)findViewById(R.id.nameBox);
        descriptionBox = (TextView)findViewById(R.id.descriptionBox);
        oldPriceFrame = (FrameLayout)findViewById(R.id.oldPriceFrame);

        likesLayout = (FrameLayout)findViewById(R.id.likesLayout);
        likeCountFrame = (FrameLayout)findViewById(R.id.countFrame);
        likeCountBox = (TextView)findViewById(R.id.countBox);

        ((TextView)findViewById(R.id.caption)).setTypeface(bold);
        priceBox.setTypeface(bold);
        brandNameBox.setTypeface(bold);
        oldPriceBox.setTypeface(normal);
        descriptionBox.setTypeface(normal);

        brandLogoBox = (CircleImageView)findViewById(R.id.brandLogoBox);
        Glide.with(this).load(Commons.brand.getLogoUrl()).into(brandLogoBox);

        storeLogoBox = (CircleImageView)findViewById(R.id.storeLogoBox);
        Glide.with(this).load(Commons.store.getLogoUrl()).into(storeLogoBox);

        brandNameBox.setText(Commons.brand.getName());
        storeNameBox.setText(Commons.store.getName());

        storeNameBox.setTypeface(normal);
        brandNameBox.setTypeface(normal);
        nameBox.setTypeface(normal);

        pager = findViewById(R.id.viewPager);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100
                / mMaxScrollSize;

        Log.d("Percentage+++", String.valueOf(currentScrollPercentage));

        if (currentScrollPercentage >= 10) {
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
        }else if (currentScrollPercentage <= 20) {
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

    private void getProductPictures(String productId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getProductPictures")
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
                            String success = response.getString("result_code");
                            Log.d("Rcode=====> :",success);
                            if (success.equals("0")) {
                                pictures.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for (int i = 0; i < dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Picture picture = new Picture();
                                    picture.setIdx(object.getInt("id"));
                                    picture.setUrl(object.getString("picture_url"));
                                    picture.setFile(null);
                                    pictures.add(picture);
                                }

                                Commons.product.set_pictureList(pictures);

                                adapter.setDatas(pictures);
                                adapter.notifyDataSetChanged();
                                pager.setAdapter(adapter);

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

    @Override
    public void onResume() {
        super.onResume();

        if(Commons.product.getLikes() > 0){
            likeCountFrame.setVisibility(View.VISIBLE);
            likeCountBox.setText(String.valueOf(Commons.product.getLikes()));
        }else {
            likeCountFrame.setVisibility(View.GONE);
        }

        toolbar.setTitle(Commons.product.getName());
        nameBox.setText(Commons.product.getName());
        if(Commons.product.getNew_price() == 0){
            oldPriceFrame.setVisibility(View.GONE);
            priceBox.setText(String.valueOf(Commons.product.getPrice()) + " USD");
        }
        else {
            oldPriceFrame.setVisibility(View.VISIBLE);
            priceBox.setText(String.valueOf(Commons.product.getNew_price()) + " USD");
            oldPriceBox.setText(String.valueOf(Commons.product.getPrice()) + " USD");
        }

        descriptionBox.setText(Commons.product.getDescription());

        if(Commons.product.getLikes() == 0)likesLayout.setVisibility(View.GONE);
        else {
            likesLayout.setVisibility(View.VISIBLE);
            likeCountBox.setText(String.valueOf(Commons.product.getLikes()));
            likesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        getProductPictures(String.valueOf(Commons.product.getIdx()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

































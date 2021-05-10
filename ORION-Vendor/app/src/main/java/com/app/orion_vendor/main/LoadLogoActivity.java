package com.app.orion_vendor.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.app.orion_vendor.R;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

public class LoadLogoActivity extends BaseActivity {

    boolean flag = false;
    String opt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getStringExtra("option") != null){
            opt = getIntent().getStringExtra("option");
        }

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flag = true;
            }
        }, 500);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //From here you can load the image however you need to, I recommend using the Glide library
                Commons.imageFile = new File(resultUri.getPath());
                Commons.files.clear();
                Commons.files.add(Commons.imageFile);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    if(opt.equals("profile")){
                        Commons.photoBox.setImageBitmap(bitmap);
                        if(Commons.background != null)Commons.background.setImageBitmap(bitmap);
                    }else {
                        Commons.logoBox.setImageBitmap(bitmap);
                    }
                    finish();
                    overridePendingTransition(0,0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("ERROR!!!", error.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(flag && Commons.imageFile == null){
            finish();
            overridePendingTransition(0,0);
        }
    }
}

























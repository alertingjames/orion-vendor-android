package com.app.orion_vendor.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
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
import com.app.orion_vendor.main.SplashActivity;
import com.app.orion_vendor.models.OrionAddress;
import com.app.orion_vendor.models.User;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.iamhabib.easy_preference.EasyPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.orion_vendor.base.BaseActivity.isValidCellPhone;
import static com.app.orion_vendor.base.BaseActivity.isValidEmail;

public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    Toolbar toolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden = false;
    FrameLayout pictureFrame;
    CircleImageView pictureBox;
    ImageView background;
    EditText nameBox, emailBox, phoneBox, addressBox;
    ImageButton mapButton;
    Button passwordButton;
    FrameLayout progressBar, settingButton;
    private View mFab1, mFab2;
    Typeface bold, normal;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, viewGroup, false);

        Commons.files.clear();
        Commons.imageFile = null;

        OrionAddress orionAddress = new OrionAddress();
        orionAddress.setAddress(Commons.thisUser.get_address());
        orionAddress.setCountry(Commons.thisUser.get_country());
        orionAddress.setArea(Commons.thisUser.get_area());
        orionAddress.setStreet(Commons.thisUser.get_street());
        orionAddress.setHouse(Commons.thisUser.get_house());
        orionAddress.setLatLng(Commons.thisUser.getLatLng());
        Commons.selectedOrionAddress = orionAddress;

        mFab1 = view.findViewById(R.id.btn_logout);
        mFab2 = view.findViewById(R.id.btn_submit);

        toolbar = (Toolbar) view.findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);
        settingButton = (FrameLayout) view.findViewById(R.id.settingLayout);

        bold = Typeface.createFromAsset(getActivity().getAssets(), "futura medium bt.ttf");
        normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        pictureFrame = (FrameLayout)view.findViewById(R.id.pictureFrame);
        pictureBox = (CircleImageView)view.findViewById(R.id.pictureBox);
        background = (ImageView) view.findViewById(R.id.background);

        emailBox = (EditText)view.findViewById(R.id.emailBox);
        nameBox = (EditText)view.findViewById(R.id.nameBox);
        phoneBox = (EditText)view.findViewById(R.id.phoneBox);
        addressBox = (EditText)view.findViewById(R.id.addressBox);
        addressBox.setEnabled(false);

        passwordButton = (Button)view.findViewById(R.id.passwordButton);
        mapButton = (ImageButton) view.findViewById(R.id.btn_map);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.textView = addressBox;
                Intent intent = new Intent(getActivity(), PickLocationActivity.class);
                startActivity(intent);
            }
        });

        nameBox.setTypeface(normal);
        emailBox.setTypeface(normal);
        phoneBox.setTypeface(normal);
        addressBox.setTypeface(normal);

        passwordButton.setTypeface(bold);

        Glide.with(getActivity()).load(Commons.thisUser.get_photoUrl()).into(pictureBox);
        Glide.with(getActivity()).load(Commons.thisUser.get_photoUrl()).into(background);
        nameBox.setText(Commons.thisUser.get_name());
        emailBox.setText(Commons.thisUser.get_email());
        phoneBox.setText(Commons.thisUser.get_phone_number());
        addressBox.setText(Commons.thisUser.get_address());

        pictureFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Commons.photoBox = pictureBox;
                Commons.background = background;
                Intent intent = new Intent(getActivity(), LoadLogoActivity.class);
                intent.putExtra("option", "profile");
                startActivity(intent);
                getActivity().overridePendingTransition(0,0);
            }
        });

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail(Commons.thisUser.get_email());
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new VendorSettingsFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.activity, fragment);
                transaction.addToBackStack(null).commit();
            }
        });

        AppBarLayout appbarLayout = (AppBarLayout) view.findViewById(R.id.flexible_example_appbar);

        //     setTitle(Commons.product.getName());

        mFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyPreference.with(getActivity(), "user_info").clearAll().save();
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                showToast("Logged Out.");
            }
        });

        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMember();
            }
        });

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();


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

                ViewCompat.animate(mFab1).scaleY(0).scaleX(0).start();
                ViewCompat.animate(mFab2).scaleY(0).scaleX(0).start();

                ((RealtimeBlurView)view.findViewById(R.id.real_time_blur_view)).setVisibility(View.VISIBLE);
                ((RealtimeBlurView)view.findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .start();
            }
        }else if (currentScrollPercentage <= 20) {
            if (mIsImageHidden) {
                mIsImageHidden = false;

                ViewCompat.animate(mFab1).scaleY(1).scaleX(1).start();
                ViewCompat.animate(mFab2).scaleY(1).scaleX(1).start();

                ((RealtimeBlurView)view.findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .start();
                ((RealtimeBlurView)view.findViewById(R.id.real_time_blur_view)).setVisibility(View.GONE);
            }
        }
    }


    private void sendMail(String email){

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "forgotpassword")
                .addBodyParameter("email", email)
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
                            if (result.equals("0")) {
                                showToast("We have sent a password reset link to your email. Please check...");
                                openMail(email);
                            } else if(result.equals("1")){
                                showToast("You haven't been registered. Please sign up.");
                            } else {
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

    public void openMail(String email){
        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.setType("message/rfc822");

        PackageManager pm = getActivity().getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");


        Intent openInChooser = Intent.createChooser(emailIntent, "Open As...");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if(packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if(packageName.contains("android.gm")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if(packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.setType("message/rfc822");
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
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


    private void updateMember(){

        if(nameBox.getText().length() == 0){
            showToast("Enter your name.");
            return;
        }

        if(emailBox.getText().length() == 0){
            showToast("Enter your email.");
            return;
        }

        if(!isValidEmail(emailBox.getText().toString().trim())){
            showToast("The email is invalid. Please enter a valid email.");
            return;
        }

        if(phoneBox.getText().length() == 0){
            showToast("Enter your phone number.");
            return;
        }

        if(!isValidCellPhone(phoneBox.getText().toString().trim())){
            showToast("The phone number is invalid. Please enter a valid phone number.");
            return;
        }

        if(addressBox.getText().length() == 0){
            showToast("Enter your address");
            return;
        }

        updateMember(Commons.files, nameBox.getText().toString().trim(), emailBox.getText().toString().trim(), phoneBox.getText().toString().trim(), Commons.selectedOrionAddress);

    }

    public void updateMember(ArrayList<File> files, String name, String email, String phone, OrionAddress address) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "updatemember")
                .addMultipartFileList("files", files)
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addMultipartParameter("name", name)
                .addMultipartParameter("email", email)
                .addMultipartParameter("phone_number", phone)
                .addMultipartParameter("address", address.getAddress())
                .addMultipartParameter("country", address.getCountry())
                .addMultipartParameter("area", address.getArea())
                .addMultipartParameter("street", address.getStreet())
                .addMultipartParameter("house", address.getHouse())
                .addMultipartParameter("latitude", String.valueOf(address.getLatLng().latitude))
                .addMultipartParameter("longitude", String.valueOf(address.getLatLng().longitude))
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
                                user.set_acc_status(Commons.thisUser.get_acc_status());

                                Commons.thisUser = user;
                                showToast("Your profile has been updated.");

                                EasyPreference.with(getActivity(), "user_info")
                                        .addString("email", Commons.thisUser.get_email())
                                        .addString("role", Commons.thisUser.getRole())
                                        .save();

                                getActivity().onBackPressed();

                            }else if(result.equals("1")){
                                showToast("Your account doesn't exist.");
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






































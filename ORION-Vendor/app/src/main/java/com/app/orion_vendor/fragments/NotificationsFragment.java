package com.app.orion_vendor.fragments;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.NotificationListAdapter;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.base.BaseFragmentActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.Notification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class NotificationsFragment extends Fragment {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;
    ListView list;
    FrameLayout progressBar;

    ArrayList<Notification> notifications = new ArrayList<>();
    NotificationListAdapter adapter = new NotificationListAdapter(this);

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_notifications, viewGroup, false);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "futura medium bt.ttf");
        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        progressBar = (FrameLayout) view.findViewById(R.id.loading_bar);
        title = (TextView)view.findViewById(R.id.title);

        searchBar = (LinearLayout)view.findViewById(R.id.search_bar);
        searchButton = (ImageView)view.findViewById(R.id.searchButton);
        cancelButton = (ImageView)view.findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)view.findViewById(R.id.edt_search);
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

        list = (ListView) view.findViewById(R.id.list);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelButton.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.GONE);
                searchBar.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelButton.setVisibility(View.GONE);
                searchButton.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
            }
        });

        getNotifications();

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
    public void onResume() {
        super.onResume();

    }

    private void getNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getNotifications")
                .addBodyParameter("receiver_id", String.valueOf(Commons.thisUser.get_idx()))
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
                                notifications.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Notification notification = new Notification();
                                    notification.setId(object.getInt("id"));
                                    notification.setReceiver_id(object.getInt("receiver_id"));
                                    notification.setSender_id(object.getInt("sender_id"));
                                    notification.setSender_name(object.getString("sender_name"));
                                    notification.setSender_email(object.getString("sender_email"));
                                    notification.setSender_phone(object.getString("sender_phone"));
                                    notification.setDate_time(object.getString("date_time"));
                                    notification.setMessage(object.getString("message"));
                                    notification.setImage(object.getString("image_message"));

                                    notifications.add(notification);
                                }

                                if(notifications.isEmpty())((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.GONE);

                                adapter.setDatas(notifications);
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

    public void deleteNotification(Notification notification){
        ((BaseFragmentActivity) getActivity()).showAlertDialogForQuestion("Warning", "Are you sure you want to delete this message?", getActivity(), null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                progressBar.setVisibility(View.VISIBLE);
                AndroidNetworking.post(ReqConst.SERVER_URL + "delNotification")
                        .addBodyParameter("notification_id", String.valueOf(notification.getId()))
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
                                        int index = notifications.indexOf(notification);
                                        notifications.remove(index);

                                        adapter.setDatas(notifications);
                                        if(adapter.getCount() == 0){
                                            ((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                        }else ((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                        list.setAdapter(adapter);
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

}











































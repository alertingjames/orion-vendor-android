package com.app.orion_vendor.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.orion_vendor.R;
import com.app.orion_vendor.adapters.RatingListAdapter;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.ReqConst;
import com.app.orion_vendor.models.Rating;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoreRatingsFragment extends Fragment {

    private int mPage;

    RatingBar ratingBar;
    TextView ratingsBox;
    TextView reviewsBox;
    TextView label;
    ListView ratingsList;
    FrameLayout progressBar;
    ArrayList<Rating> ratings = new ArrayList<>();
    RatingListAdapter ratingListAdapter = null;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_store_ratings, viewGroup, false);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "futura medium bt.ttf");
        Typeface normal = Typeface.createFromAsset(getActivity().getAssets(), "futura book font.ttf");

        ratingBar = (RatingBar)view.findViewById(R.id.ratingbar);
        ratingsBox = (TextView)view.findViewById(R.id.ratings);
        reviewsBox = (TextView)view.findViewById(R.id.reviews);
        label = (TextView)view.findViewById(R.id.lb);
        label.setTypeface(normal);
        progressBar = (FrameLayout)view.findViewById(R.id.loading_bar);

        ratingsList = (ListView)view.findViewById(R.id.ratingsList);

        getRatings();

        return view;
    }

    public static StoreRatingsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("Brand Details", page);
        StoreRatingsFragment fragment = new StoreRatingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt("Product Ratings");
        Log.d("Pager NO", String.valueOf(mPage));
    }

    private void getRatings(){

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getRatings")
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
                                ratings.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Rating rating = new Rating();
                                    rating.setIdx(object.getInt("id"));
                                    rating.setStoreId(object.getInt("store_id"));
                                    rating.setUserId(object.getInt("member_id"));
                                    rating.setUserName(object.getString("member_name"));
                                    rating.setUserPictureUrl(object.getString("member_photo"));
                                    rating.setRating(Float.parseFloat(object.getString("rating")));
                                    rating.setSubject(object.getString("subject"));
                                    rating.setDescription(object.getString("description"));
                                    rating.setDate(object.getString("date_time"));

                                    ratings.add(rating);
                                }

                                if(ratings.isEmpty())((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)view.findViewById(R.id.no_result)).setVisibility(View.GONE);

                                if(getActivity() != null){
                                    ratingListAdapter = new RatingListAdapter(getActivity());
                                    ratingListAdapter.setDatas(ratings);
                                    ratingsList.setAdapter(ratingListAdapter);
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
                        progressBar.setVisibility(View.GONE);
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












































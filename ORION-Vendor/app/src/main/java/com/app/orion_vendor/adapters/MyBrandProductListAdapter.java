package com.app.orion_vendor.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.main.MyProductDetailActivity;
import com.app.orion_vendor.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyBrandProductListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<Product> _datas = new ArrayList<>();
    private ArrayList<Product> _alldatas = new ArrayList<>();

    public MyBrandProductListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Product> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_product_list, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.pictureBox);
            holder.price = (TextView) convertView.findViewById(R.id.priceBox);
            holder.oldPrice= (TextView) convertView.findViewById(R.id.oldPriceBox);
            holder.oldPriceFrame= (FrameLayout) convertView.findViewById(R.id.oldPriceFrame);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.ratingbar);
            holder.ratings = (TextView) convertView.findViewById(R.id.ratings);
            holder.likes = (TextView) convertView.findViewById(R.id.likes);
            holder.deleteButton = (ImageView) convertView.findViewById(R.id.btn_delete);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Product entity = (Product) _datas.get(position);

        if(entity.getNew_price() == 0){
            holder.oldPriceFrame.setVisibility(View.GONE);
            holder.price.setText(String.valueOf(entity.getPrice()) + " SGD");
        }
        else {
            holder.oldPriceFrame.setVisibility(View.VISIBLE);
            holder.price.setText(String.valueOf(entity.getNew_price()) + " SGD");
            holder.oldPrice.setText(String.valueOf(entity.getPrice()) + " SGD");
        }

        Typeface book = Typeface.createFromAsset(_context.getAssets(), "futura book font.ttf");
        holder.price.setTypeface(book);
        holder.oldPrice.setTypeface(book);

        holder.likes.setText(String.valueOf(entity.getLikes()));
        holder.ratings.setText(String.valueOf(entity.getRatings()));
        holder.ratingBar.setRating(entity.getRatings());

        if(entity.getPicture_url().length() > 0){
            View finalConvertView = convertView;
            Picasso.with(_context)
                    .load(entity.getPicture_url())
                    .into(holder.picture, new Callback() {
                        @Override
                        public void onSuccess() {
                            ((ProgressBar) finalConvertView.findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            ((ProgressBar) finalConvertView.findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                        }
                    });
        }

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Commons.myBrandProductsFragment != null){
                    Commons.myBrandProductsFragment.delProduct(String.valueOf(entity.getIdx()));
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.product = entity;
                Intent intent = new Intent(_context, MyProductDetailActivity.class);
                _context.startActivity(intent);
            }
        });

        return convertView;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {
            for (Product product : _alldatas){

                if (product instanceof Product) {

                    String value = ((Product) product).getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(product);
                    }else {
                        value = ((Product) product).getCategory().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(product);
                        }else {
                            value = String.valueOf(((Product) product).getPrice());
                            if (value.contains(charText)) {
                                _datas.add(product);
                            }else {
                                value = ((Product) product).getDescription().toLowerCase();
                                if (value.contains(charText)) {
                                    _datas.add(product);
                                }else {
                                    value = ((Product) product).getGender().toLowerCase();
                                    if (value.contains(charText)) {
                                        _datas.add(product);
                                    }else {
                                        value = ((Product) product).getGenderKey().toLowerCase();
                                        if (value.contains(charText)) {
                                            _datas.add(product);
                                        }else {
                                            value = ((Product) product).getSubcategory().toLowerCase();
                                            if (value.contains(charText)) {
                                                _datas.add(product);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        ImageView picture;
        TextView price, oldPrice;
        FrameLayout oldPriceFrame;
        RatingBar ratingBar;
        TextView ratings, likes;
        ImageView deleteButton;
    }
}

































package com.app.orion_vendor.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.main.MyBrandDetailActivity;
import com.app.orion_vendor.models.Brand;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BrandListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<Brand> _datas = new ArrayList<>();
    private ArrayList<Brand> _alldatas = new ArrayList<>();

    public BrandListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Brand> datas) {

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
            convertView = inflater.inflate(R.layout.item_brand_list, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.pictureBox);
            holder.name = (TextView) convertView.findViewById(R.id.nameBox);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Brand entity = (Brand) _datas.get(position);

        holder.name.setText(entity.getName());

        Typeface bold = Typeface.createFromAsset(_context.getAssets(), "futura medium bt.ttf");
        holder.name.setTypeface(bold);

        Picasso.with(_context).setLoggingEnabled(true);

        if(entity.getLogoUrl().length() > 0){
            View finalConvertView = convertView;
            Picasso.with(_context)
                    .load(entity.getLogoUrl()).error(R.drawable.noresult)
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

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.brand = entity;
                Intent intent = new Intent(_context, MyBrandDetailActivity.class);
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
            for (Brand brand : _alldatas){

                if (brand != null) {

                    String value = brand.getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(brand);
                    }else {
                        value = brand.getCategory().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(brand);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        ImageView picture;
        TextView name;
    }
}























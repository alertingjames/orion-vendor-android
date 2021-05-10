package com.app.orion_vendor.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.main.DriverDetailActivity;
import com.app.orion_vendor.main.MyBrandDetailActivity;
import com.app.orion_vendor.models.Brand;
import com.app.orion_vendor.models.User;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DriverListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<User> _datas = new ArrayList<>();
    private ArrayList<User> _alldatas = new ArrayList<>();
    public static DecimalFormat df = new DecimalFormat("0.00");

    public DriverListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<User> datas) {

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
            convertView = inflater.inflate(R.layout.item_drivers, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.pictureBox);
            holder.name = (TextView) convertView.findViewById(R.id.nameBox);
            holder.addressBox = (TextView) convertView.findViewById(R.id.addressBox);
            holder.statusBox = (TextView) convertView.findViewById(R.id.statusBox);
            holder.distanceBox = (TextView) convertView.findViewById(R.id.distanceBox);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final User entity = (User) _datas.get(position);

        holder.name.setText(entity.get_name());

        Typeface bold = Typeface.createFromAsset(_context.getAssets(), "futura medium bt.ttf");
        Typeface normal = Typeface.createFromAsset(_context.getAssets(), "futura book font.ttf");

        holder.name.setTypeface(bold);
        holder.addressBox.setText(entity.get_address());
        holder.addressBox.setTypeface(normal);

        holder.statusBox.setText(entity.get_status().length() > 0?"Available":"Unavailable");
        holder.statusBox.setTextColor(entity.get_status().length() > 0?_context.getColor(R.color.green):_context.getColor(R.color.gray));
        holder.statusBox.setTypeface(normal);

        holder.distanceBox.setText(String.valueOf(df.format(entity.getDistance()/1000)) + " km");

        if(entity.get_photoUrl().length() > 0){
            View finalConvertView = convertView;
            Glide.with(_context)
                    .load(entity.get_photoUrl()).error(R.drawable.logo2)
                    .into(holder.picture);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity.get_status().equals("available")){
                    Commons.user = entity;
                    Intent intent = new Intent(_context, DriverDetailActivity.class);
                    _context.startActivity(intent);
                }
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
            for (User user : _alldatas){

                if (user != null) {

                    String value = user.get_name().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(user);
                    }else {
                        value = user.get_address().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(user);
                        }else {
                            value = user.get_phone_number().toLowerCase();
                            if (value.contains(charText)) {
                                _datas.add(user);
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
        TextView name, addressBox, statusBox, distanceBox;
    }
}




















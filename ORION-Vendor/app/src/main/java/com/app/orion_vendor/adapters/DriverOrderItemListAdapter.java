package com.app.orion_vendor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;

import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.commons.Constants;
import com.app.orion_vendor.main.DriverListActivity;
import com.app.orion_vendor.main.ViewImageActivity;
import com.app.orion_vendor.models.OrderItem;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DriverOrderItemListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<OrderItem> _datas = new ArrayList<>();
    private ArrayList<OrderItem> _alldatas = new ArrayList<>();
    public static DecimalFormat df = new DecimalFormat("0.00");

    public DriverOrderItemListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<OrderItem> datas) {

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
            convertView = inflater.inflate(R.layout.item_driver_orders, parent, false);

            holder.orderIDBox = (TextView) convertView.findViewById(R.id.orderIDBox);
            holder.categoryBox = (TextView) convertView.findViewById(R.id.categoryBox);
            holder.priceBox = (TextView) convertView.findViewById(R.id.priceBox);
            holder.quantityBox = (TextView) convertView.findViewById(R.id.quantityBox);
            holder.pictureBox = (ImageView) convertView.findViewById(R.id.pictureBox);
            holder.genderBox = (TextView) convertView.findViewById(R.id.genderBox);
            holder.addressBox = (TextView) convertView.findViewById(R.id.addressBox);
            holder.productNameBox = (TextView) convertView.findViewById(R.id.productNameBox);
            holder.statusBox = (TextView) convertView.findViewById(R.id.statusBox);
            holder.addressLineBox = (TextView) convertView.findViewById(R.id.addressLineBox);

            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final OrderItem entity = (OrderItem) _datas.get(position);

        Typeface normal = Typeface.createFromAsset(_context.getAssets(), "futura book font.ttf");
        Typeface bold = Typeface.createFromAsset(_context.getAssets(), "futura medium bt.ttf");
        holder.priceBox.setTypeface(bold);
        holder.quantityBox.setTypeface(bold);
        holder.productNameBox.setTypeface(bold);
        holder.categoryBox.setTypeface(normal);
        holder.genderBox.setTypeface(normal);
        holder.addressBox.setTypeface(normal);
        holder.addressLineBox.setTypeface(normal);
        holder.orderIDBox.setTypeface(normal);
        holder.statusBox.setTypeface(normal);

        holder.orderIDBox.setText(entity.getOrderID());
        holder.priceBox.setText(df.format(entity.getPrice()) + " " + Constants.currency);
        holder.productNameBox.setText(entity.getProduct_name());
        holder.categoryBox.setText(entity.getCategory() + "|" + entity.getSubcategory());
        holder.quantityBox.setText("QTY: " + String.valueOf(entity.getQuantity()));
        holder.genderBox.setText(entity.getGender());
        holder.addressBox.setText(entity.getAddress());
        holder.addressLineBox.setText(entity.getAddress_line());

        if(entity.getStatus().length() > 0){
            holder.statusBox.setText(Commons.orderStatus.statusStr.get(entity.getStatus()));
        }

        if(entity.getPicture_url().length() > 0){
            Picasso.with(_context)
                    .load(entity.getPicture_url())
                    .into(holder.pictureBox);
        }

        holder.pictureBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.orderItem = entity;
                Commons.ordersActivity.getOrder(entity);
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
            for (OrderItem item : _alldatas){
                if (item instanceof OrderItem) {
                    String value = ((OrderItem) item).getOrderID().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(item);
                    }else {
                        value = ((OrderItem) item).getCategory().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(item);
                        }else {
                            value = String.valueOf(((OrderItem) item).getPrice());
                            if (value.contains(charText)) {
                                _datas.add(item);
                            }else {
                                value = ((OrderItem) item).getSubcategory().toLowerCase();
                                if (value.contains(charText)) {
                                    _datas.add(item);
                                }else {
                                    value = ((OrderItem) item).getStore_name().toLowerCase();
                                    if (value.contains(charText)) {
                                        _datas.add(item);
                                    }else {
                                        value = ((OrderItem) item).getCategory().toLowerCase();
                                        if (value.contains(charText)) {
                                            _datas.add(item);
                                        }else {
                                            value = ((OrderItem) item).getProduct_name().toLowerCase();
                                            if (value.contains(charText)) {
                                                _datas.add(item);
                                            }else {
                                                value = ((OrderItem) item).getGender().toLowerCase();
                                                if (value.contains(charText)) {
                                                    _datas.add(item);
                                                }else {
                                                    value = String.valueOf(((OrderItem) item).getQuantity());
                                                    if (value.contains(charText)) {
                                                        _datas.add(item);
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
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        TextView addressBox, addressLineBox, productNameBox, categoryBox;
        TextView orderIDBox, genderBox, priceBox, quantityBox, statusBox;
        ImageView pictureBox;
        CheckBox checkBox;
    }
}












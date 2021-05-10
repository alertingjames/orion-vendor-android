package com.app.orion_vendor.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.orion_vendor.R;
import com.app.orion_vendor.commons.Commons;

import java.util.ArrayList;

public class CategoryItemListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> _datas = new ArrayList<>();
    ArrayList<String> _alldatas = new ArrayList<>();

    public CategoryItemListAdapter(Context context){
        super();
        this.context = context;
    }

    public void setDatas(ArrayList<String> datas) {
        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount() {
        return _datas.size();
    }

    @Override
    public Object getItem(int i) {
        return _datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CustomHolder holder;

        if (view == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            view = inflater.inflate(R.layout.item_category_list, viewGroup, false);

            holder.itemBox = (TextView) view.findViewById(R.id.itemBox);

            view.setTag(holder);
        } else {
            holder = (CustomHolder) view.getTag();
        }

        Typeface normal = Typeface.createFromAsset(context.getAssets(), "futura book font.ttf");

        final String entity = (String) _datas.get(i);
        holder.itemBox.setText(entity);
        holder.itemBox.setTypeface(normal);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.textView.setText(entity);
                Commons.layout.setVisibility(View.GONE);
            }
        });

        return view;
    }

    class CustomHolder {

        TextView itemBox;
    }
}






























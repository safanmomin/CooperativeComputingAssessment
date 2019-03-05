package com.example.ccapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ccapp.DataItem.Restaurants;
import com.example.ccapp.R;

import java.util.ArrayList;

public class RestaurantsAdapter extends BaseAdapter {
    private ArrayList<Restaurants> list;
    private LayoutInflater layoutInflater;

    public RestaurantsAdapter(Context context, ArrayList<Restaurants> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = layoutInflater.inflate(R.layout.restaurant_listview, null);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView type = (TextView) view.findViewById(R.id.type);
            ImageView image = (ImageView) view.findViewById(R.id.image);

            name.setText(((Restaurants)list.get(i)).getRestaurantTitle());
            type.setText(((Restaurants)list.get(i)).getRestaurantType());

            if(((Restaurants)list.get(i)).getRestaurantID().matches("2")){
                image.setImageResource(R.drawable.res2);
            }else if(((Restaurants)list.get(i)).getRestaurantID().matches("3")){
                image.setImageResource(R.drawable.res3);
            }else if(((Restaurants)list.get(i)).getRestaurantID().matches("4")){
                image.setImageResource(R.drawable.res4);
            }
        }
        return view;
    }
}

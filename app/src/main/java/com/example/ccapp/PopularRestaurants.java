package com.example.ccapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.example.ccapp.Adapters.RestaurantsAdapter;
import com.example.ccapp.DataItem.Restaurants;

import java.util.ArrayList;

public class PopularRestaurants extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_restaurants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = (ListView) findViewById(R.id.listview);

        ArrayList<Restaurants> restaurants = new ArrayList<Restaurants>();
        restaurants.add(new Restaurants("1", "Rosati Bistro", "Continental"));
        restaurants.add(new Restaurants("2", "Pranzo", "Italian"));
        restaurants.add(new Restaurants("3", "Cocochan", "Japanese"));
        restaurants.add(new Restaurants("4", "Cafe Flo", "French"));
        listView.setAdapter(new RestaurantsAdapter(PopularRestaurants.this, restaurants));

    }

}

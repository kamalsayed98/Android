package com.example.kamal.flee5;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.widget.TextView;

import static android.content.pm.ActivityInfo.COLOR_MODE_HDR;


public class DelivaryPop extends Activity {


        TextView road_city,longtitude,latitude;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onCreate( Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.delivary_pop);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int hight = dm.heightPixels;
            getWindow().setLayout((int)(width*0.8),(int)(hight*0.8));

            road_city = (TextView)findViewById(R.id.roat_city);
            latitude = (TextView)findViewById(R.id.latitude);
            longtitude = (TextView)findViewById(R.id.longtitude);

            Intent intent = getIntent();
            double lng = intent.getDoubleExtra("lng",0);
            longtitude.setText(String.valueOf(lng));
            double lat = intent.getDoubleExtra("lat",0);
            latitude.setText(String.valueOf(lat));
            String city;
            try{
                city = intent.getStringExtra("city");

            }catch (Exception e){
                city = "undefined city";
            }
            road_city.setText(city);

        }
    }

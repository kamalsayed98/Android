package com.example.kamal.flee5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;


public class pop extends Activity {

    SharedPreferences pref ;
    SharedPreferences.Editor editor ;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int hight = dm.heightPixels;
        getWindow().setLayout((int)(width*0.6),(int)(hight*0.6));

        pref= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        editor= pref.edit();

    }
    public void lastOrder(View v){
        startActivity(new Intent(this,ClientLastOrder.class));

    }
    public void payment(View v){
        startActivity(new Intent(this,ClientPayment.class));
    }

    public void account(View v){
        startActivity(new Intent(this,ClientAccount.class));

    }
    public void about(View v){
        startActivity(new Intent(this,ClientAbout.class));

    }
    public void logout(View v){
        editor.clear();
        editor.commit();
        startActivity(new Intent(this,ChoosePage.class));
    }
}

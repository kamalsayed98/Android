package com.example.kamal.flee5;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static TextView mainTextView;
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    public static final String USER_SHARED_PREFERENCES = "user_pref";

    SharedPreferences prefs ;
    String username = "";
    String user = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTextView = (TextView)findViewById(R.id.mainTextView);

       // startActivity(new Intent(this,MapsClient.class));
        if(isServecesOk())  {   openCurrentPage();    }
    }



    public boolean isServecesOk(){
        Log.d(TAG ,"isServecesOk: checking google serveces virsio");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available == ConnectionResult.SUCCESS)
        {
            //every thing is fine
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this , available , ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"you can't make map request",Toast.LENGTH_LONG).show();
        }
        return  false;
    }

    private void openCurrentPage() {
        prefs = getSharedPreferences(USER_SHARED_PREFERENCES, MODE_PRIVATE);

        username = prefs.getString("username", "");
        user = prefs.getString("user", "");

        if(!username.equals(""))
        {
            if(user.equals("Client"))
            {
                startActivity(new Intent(this,MapsClient.class));
            }
            else
            {
                if(user.equals("Driver")){
                    startActivity(new Intent(this,MapsDriver.class));

                }
            }
        }
        else
        {
            startActivity(new Intent(this,ChoosePage.class));
        }
    }
}

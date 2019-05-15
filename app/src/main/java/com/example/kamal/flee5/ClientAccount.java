package com.example.kamal.flee5;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ClientAccount extends AppCompatActivity {
    TextView id,name,username,password,phoneNumber;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_account);

        prefs = getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE);

        id = (TextView)findViewById(R.id.id);
        name = (TextView)findViewById(R.id.name);
        username = (TextView)findViewById(R.id.username);
        password = (TextView)findViewById(R.id.password);
        phoneNumber = (TextView)findViewById(R.id.phoneNumber);

        id.setText(String.valueOf(prefs.getInt("id",0)));
        name.setText(prefs.getString("name",""));
        username.setText(prefs.getString("username",""));
        phoneNumber.setText(prefs.getString("phoneNumber",""));
        String pass =prefs.getString("password","");
        int i = pass.length()/2;
        String npass = pass.substring(0,i);
        for(int j=0;j<i;j++)
        npass+='*';
        password.setText(npass);


    }
}

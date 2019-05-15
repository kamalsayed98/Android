package com.example.kamal.flee5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClientRigister extends AppCompatActivity {

    EditText name,username,password,conformPassword,phone,adress,birthday;
    SharedPreferences.Editor editor;
    String error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_rigister);
        name =(EditText)findViewById(R.id.name);
        username =(EditText)findViewById(R.id.username);
        password =(EditText)findViewById(R.id.password);
        conformPassword =(EditText)findViewById(R.id.conformPassword);
        phone =(EditText)findViewById(R.id.phone);
        adress =(EditText)findViewById(R.id.adress);
        birthday =(EditText)findViewById(R.id.birthday);
        editor= getSharedPreferences("myPerf", MODE_PRIVATE).edit();

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*



                check if the username exists



                */

            }
        });
    }

    public void Rigister(View v){
        if(!password.getText().toString().equals(conformPassword.getText().toString())){
            Toast.makeText(this,"password fileds are not the same",Toast.LENGTH_LONG).show();
            return;
        }
        if(password.getText().toString().length()<6){
            Toast.makeText(this,"password must be at least 6 charachers",Toast.LENGTH_LONG).show();
            return;
        }
        if(name.getText().toString().equals("") || username.getText().toString().equals("") ||
                phone.getText().toString().equals("") || adress.getText().toString().equals("") ||
                    birthday.getText().toString().equals(""))
        {
            Toast.makeText(this,"all Fields must be entered",Toast.LENGTH_LONG).show();
            return;
        }


        /* rigister  the client  */
        if(checkNetworkConnection()){
            new HTTPAsyncTask().execute("http://abdullahhaidar92-001-site1.etempurl.com/api/Clients/PostClient");

        }
    }


    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (!(networkInfo != null && (isConnected = networkInfo.isConnected()))) {
            Toast.makeText(this,"phone is not connected",Toast.LENGTH_SHORT).show();
        }
        return isConnected;
    }

    public class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpPost(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Unable to retrieve web page.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                editor.putString("username",username.getText().toString());
                editor.putString("user","Client");
                editor.apply();
                editor.commit();
                goToClientPage();
            } else {
                loginDeny(result);
            }
        }
    }

         private void loginDeny(String result) {
            username.setText("");
            password.setText("");
            conformPassword.setText("");
            Toast.makeText(this,"username exists",Toast.LENGTH_LONG).show();
        }

         private void goToClientPage() {

        startActivity(new Intent(this,MapsClient.class));
        }

        private String HttpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        // 2. build JSON object
        JSONObject jsonObject = buidJsonObject();
        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);
        // 4. make POST request to the given URL
        conn.connect();
        // 5. return response message

        return conn.getResponseMessage()+"";
        }

        private String getJsonFile(HttpURLConnection conn) throws IOException {
        BufferedReader reader = null;

        InputStream stream = conn.getInputStream();

        reader = new BufferedReader(new InputStreamReader(stream));

        StringBuffer buffer = new StringBuffer();
        String line = "";
        String data = "";

        while ((line = reader.readLine()) != null) {
        buffer.append(line + "\n");
        Log.d("Response: ", "> " + line);

        }
        return buffer.toString();
        }

        private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("name",  name.getText().toString());
            jsonObject.accumulate("username", username.getText().toString());
             jsonObject.accumulate("password",  password.getText().toString());
             jsonObject.accumulate("adress",  adress.getText().toString());
             jsonObject.accumulate("phonenumber",  phone.getText().toString());
             jsonObject.accumulate("birthdate",  birthday.getText().toString());

        return jsonObject;
        }

        private void setPostRequestContent(HttpURLConnection conn,
        JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
        }


}



package com.example.kamal.flee5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class DriverLoginPage extends AppCompatActivity {
    EditText username,password;
    SharedPreferences.Editor editor;
    String jsonString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_page);
        editor= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE).edit();
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.etPassword);


    }

    public void Login(View v){
        if(checkNetworkConnection()){
            new HTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/Drivers/GetDriver");
        }
    }
    public void showPassword(View v){
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
                 goToDriverPage();
            } else {
                loginDeny(result);
            }
        }
    }

    private void loginDeny(String result) {
        password.setText("");
        Toast.makeText(this,result,Toast.LENGTH_LONG).show();
    }

    private void goToDriverPage() {
        editor.putString("username",username.getText().toString());
        editor.putString("password",password.getText().toString());
        editor.putString("user","Driver");

        try {

            JSONObject obj = new JSONObject(jsonString);

            Log.d("My App", obj.toString());

            editor.putInt("id",obj.getInt("id"));

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
        }

        editor.apply();
        startActivity(new Intent(this,MapsDriver.class));
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

        jsonString = getJsonFile(conn);

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
        jsonObject.accumulate("username", username.getText().toString());
        jsonObject.accumulate("password",  password.getText().toString());

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

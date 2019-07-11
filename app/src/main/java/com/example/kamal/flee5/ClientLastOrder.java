package com.example.kamal.flee5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

public class ClientLastOrder extends AppCompatActivity {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    String jsonString = "";
    TextView deliveryId,quentity,deliveryTime,sourceCity,sourceLat,sourceLng,destCity,destLat,destLng,isStarted,isAnswered,isFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        editor= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE).edit();

        if(prefs.getInt("deliveryId",0) == 0){
            setContentView(R.layout.activiity_client_last_order_2);
            return;
        }

        setContentView(R.layout.activity_client_last_order);
        deliveryId = (TextView)findViewById(R.id.deliveryId);
        quentity = (TextView)findViewById(R.id.quentity);
        deliveryTime = (TextView)findViewById(R.id.deliveryTime);
        sourceCity = (TextView)findViewById(R.id.sourceCity);
        sourceLat = (TextView)findViewById(R.id.sourceLat);
        sourceLng = (TextView)findViewById(R.id.sourceLng);
        destCity = (TextView)findViewById(R.id.destCity);
        destLat = (TextView)findViewById(R.id.destLat);
        destLng = (TextView)findViewById(R.id.destLng);
        isStarted = (TextView)findViewById(R.id.started);
        isAnswered = (TextView)findViewById(R.id.answered);
        isFinished = (TextView)findViewById(R.id.finished);


        if(checkNetworkConnection()){
            new ClientLastOrder.HTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/Deliveries/GetClientDelivery");
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
                try{
                JSONObject obj = new JSONObject(jsonString);
                deliveryId.setText(String.valueOf(obj.getInt("id")));
                quentity.setText(String.valueOf(obj.getInt("quantity")));
                deliveryTime.setText(obj.getString("time"));
                sourceCity.setText(obj.getString("sourceCity"));
                sourceLat.setText(String.valueOf(obj.getDouble("sourceLatitude")));
                sourceLng.setText(String.valueOf(obj.getDouble("sourceLongtitude")));
                destCity.setText(obj.getString("destinationCity"));
                destLat.setText(String.valueOf(obj.getDouble("destinationLatitude")));
                destLng.setText(String.valueOf(obj.getDouble("destinationLongtitude")));
                isStarted.setText(String.valueOf(obj.getBoolean("started")));
                isAnswered.setText(String.valueOf(obj.getBoolean("answered")));
                isFinished.setText(String.valueOf(obj.getBoolean("finished")));

                if(isStarted.getText().toString().equals("false")){
                    isStarted.setTextColor(Color.RED);
                }else{
                    isStarted.setTextColor(Color.GREEN);
                }
                if(isAnswered.getText().toString().equals("false")){
                    isAnswered.setTextColor(Color.RED);
                }else{
                    isAnswered.setTextColor(Color.GREEN);
                }
                if(isFinished.getText().toString().equals("false")){
                    isFinished.setTextColor(Color.RED);
                }else{
                    isFinished.setTextColor(Color.GREEN);
                    editor.putInt("deliveryId",0);
                }
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
                }

            } else {
                Toast.makeText(ClientLastOrder.this,result,Toast.LENGTH_LONG).show();
            }
        }
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

         getJsonFile(conn);

        return conn.getResponseMessage()+"";
    }
    private void getJsonFile(HttpURLConnection conn) throws IOException {
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

        jsonString = buffer.toString();

    }

    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("id", prefs.getInt("deliveryId",0));
        jsonObject.accumulate("username", prefs.getString("username",""));
        jsonObject.accumulate("password",  prefs.getString("password",""));

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
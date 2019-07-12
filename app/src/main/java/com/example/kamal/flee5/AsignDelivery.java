package com.example.kamal.flee5;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AsignDelivery extends AppCompatActivity {

    double srcLat,srcLng,destLat,destLng;
    SharedPreferences prefs ;
    EditText quentity;
    Spinner companies;
    String srcCity,destCity,currentDateandTime;
    TextView source,destination;
    FloatingActionButton sourceFloatingAction,destinationFloatingAction;
    ArrayAdapter<String> adapter;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asign_delivery);

        prefs= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        quentity = (EditText)findViewById(R.id.quantity);
        companies = (Spinner)findViewById(R.id.companies);
        final Intent intent = getIntent();
        srcLat = intent.getDoubleExtra("srcLat",0);
        srcLng = intent.getDoubleExtra("srcLng",0);
        destLat = intent.getDoubleExtra("destLat",0);
        destLng = intent.getDoubleExtra("destLng",0);



        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> srcAddresses = geocoder.getFromLocation(srcLat, srcLng, 1);
            srcCity = srcAddresses.get(0).getAdminArea().substring(0,srcAddresses.get(0).getAdminArea().length()-11) + ", " + srcAddresses.get(0).getSubAdminArea();
            List<Address> destAddresses = geocoder.getFromLocation(destLat, destLng, 1);
            destCity =  destAddresses.get(0).getAdminArea().substring(0,destAddresses.get(0).getAdminArea().length()-11) + ", " + destAddresses.get(0).getSubAdminArea();

        }catch (IOException e) {
                e.printStackTrace();
            }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        currentDateandTime = sdf.format(new Date());
        Log.d("time",currentDateandTime);
        source = (TextView)findViewById(R.id.source);
        destination = (TextView)findViewById(R.id.destination);
        source.setText(srcCity);
        destination.setText(destCity);
        sourceFloatingAction = (FloatingActionButton)findViewById(R.id.sourceFloatingAction);
        destinationFloatingAction = (FloatingActionButton)findViewById(R.id.destinationFloatingAction);


        final Intent popIntent = new Intent(this,DelivaryPop.class);

        sourceFloatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popIntent.putExtra("city",source.getText());
                popIntent.putExtra("lng",srcLng);
                popIntent.putExtra("lat",srcLat);
                startActivity(popIntent);

            }
        });
        destinationFloatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                popIntent.putExtra("city",destination.getText());
                popIntent.putExtra("lng",destLng);
                popIntent.putExtra("lat",destLat);
                startActivity(popIntent);
            }
        });

        editor= getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE).edit();

        if(checkNetworkConnection()){
            new AsignDelivery.HTTPAsyncTask1().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/Companies/Names");
        }
    }
    public class HTTPAsyncTask1 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpPost1(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Unable to retrieve web page.";
            }
        }
    }
        private String HttpPost1(String myUrl) throws IOException, JSONException {
            String result = "";

            URL url = new URL(myUrl);

            // 1. create HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            // 2. build JSON object
            JSONObject jsonObject = new JSONObject();
            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject);
            // 4. make POST request to the given URL
            conn.connect();
            // 5. return response message

            getJsonFile1(conn);


            return conn.getResponseMessage()+"";
        }


        private void getJsonFile1(HttpURLConnection conn) throws IOException {
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
            try {
                Log.d("companies",buffer.toString());
              //  buffer.toString();
                String[] company = { "Furniture Company","Taxi Company"};//buffer.toString().substring(1,buffer.toString().length()-1).split("\"");
                adapter = new ArrayAdapter<String>(
                        AsignDelivery.this, android.R.layout.simple_spinner_item, company);


                companies.setAdapter(adapter);
             /*   companies.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                       int arg2, long arg3) {
                               *//* int position = companies.getSelectedItemPosition();
                                Toast.makeText(getApplicationContext(),"You have selected
                                        "+celebrities[+position],Toast.LENGTH_LONG).show();
// TODO Auto-generated method stub*//*
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub
                            }
                        }
                );*/


            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + buffer.toString() + "\"");
            }

            editor.apply();

        }


        //******************************************************************

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
        try {

            JSONObject obj = new JSONObject(buffer.toString());

            editor.putInt("deliveryId",obj.getInt("id"));

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + buffer.toString() + "\"");
        }

        editor.apply();

    }


    public void AssignDelivary(View v) {
        if (quentity.getText().toString().equals("")) {
            Toast.makeText(AsignDelivery.this, "enter quentity to be delivared", Toast.LENGTH_LONG).show();
            return;
        }
        if(checkNetworkConnection()){
            new AsignDelivery.HTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/Deliveries/MakeAnOrder");
        }
    }


    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (!(networkInfo != null && (isConnected = networkInfo.isConnected()))) {
            Toast.makeText(AsignDelivery.this,"phone is not connected",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AsignDelivery.this,"order is accepted",Toast.LENGTH_LONG).show();
                startActivity(new Intent(AsignDelivery.this,MapsClient.class));
            } else {
                Toast.makeText(AsignDelivery.this,result,Toast.LENGTH_LONG).show();
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
    private void setPostRequestContent(HttpURLConnection conn,
                                       JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(AsignDelivery.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("Time",currentDateandTime);
            jsonObject.accumulate("CompanyName",companies.getSelectedItem().toString());
            jsonObject.accumulate("ClientId",prefs.getInt("id",0));
            jsonObject.accumulate("SourceLongtitude",srcLng);
            jsonObject.accumulate("SourceLatitude",srcLat);
            jsonObject.accumulate("SourceCity",srcCity);
            jsonObject.accumulate("DestinationLongtitude",destLng);
            jsonObject.accumulate("Destinationlatitude",destLat);
            jsonObject.accumulate("DestinationCity",destCity);
            jsonObject.accumulate("Quantity",Double.parseDouble(quentity.getText().toString()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }



}

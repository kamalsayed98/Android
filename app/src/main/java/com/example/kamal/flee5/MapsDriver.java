package com.example.kamal.flee5;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsDriver extends FragmentActivity
        implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
        private static final int Request_User_Location_Code = 99;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private String jsonString;
    private String jsonStartDelivery;
    private String jsonFinishDelivery;
    private String jsonUpdateDelivery;
    SharedPreferences prefs ;
    SharedPreferences.Editor editor ;

    public static int routeColor =0;

    ArrayList<LatLng> markerPoints;
    private static ArrayList<Integer> deliveriesId;
    private String m_FuelLevel = "";
    private String m_StartOdometer = "";
    SimpleDateFormat sdf;
    String currentDateandTime ;
    static String currentDeliverySummatyId ;

    LocationManager manager;

    TextView logout , startDeliveries,engineOnOff;
    static int updateDeliveriesStatus =0;
    static int engineRunning =0;
    ProgressDialog progDailog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        prefs = getSharedPreferences(MainActivity.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        editor = prefs.edit();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerPoints = new ArrayList<LatLng>();
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }else{
            getDeviceLocation();
        }

        deliveriesId = new ArrayList<Integer>();
        sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        logout = (TextView) findViewById(R.id.logout);
        startDeliveries = (TextView) findViewById(R.id.startDeliveries);
        engineOnOff = (TextView) findViewById(R.id.engineOnOff);
    }

    public void drawPoints() {
        progDailog = new ProgressDialog(MapsDriver.this);
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
        progDailog.show();
        try {
            mMap.clear();
            for (int i = 0; i < (markerPoints.size()/2); i++) {
                int j = 2 * i;
                if (i == 0) {
                    MarkerOptions options = new MarkerOptions()
                            .position(markerPoints.get(j))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mMap.addMarker(options);

                } else {
                    MarkerOptions options = new MarkerOptions()
                            .position(markerPoints.get(j));
                    mMap.addMarker(options);
                }


                MarkerOptions options2 = new MarkerOptions()
                        .position(markerPoints.get(j + 1));
                mMap.addMarker(options2);

                LatLng origin = markerPoints.get(j);
                LatLng dest = markerPoints.get(j + 1);

                String url = getDirectionsUrl(origin, dest);

                MapsDriver.DownloadTask downloadTask = new MapsDriver.DownloadTask();
                downloadTask.execute(url);
            }


            for (int i = 0; i < (markerPoints.size()/2)- 1; i++) {
                int j = (2 * i) + 1;
                LatLng origin = markerPoints.get(j);
                LatLng dest = markerPoints.get(j + 1);

                String url = getDirectionsUrl(origin, dest);

                MapsDriver.DownloadTask downloadTask = new MapsDriver.DownloadTask();
                downloadTask.execute(url);
            }


            LatLng origin = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            LatLng dest = markerPoints.get(0);

            String url = getDirectionsUrl(origin, dest);

            MapsDriver.DownloadTask1 downloadTask1 = new MapsDriver.DownloadTask1();
            downloadTask1.execute(url);


        }  catch (Throwable t) {
        Log.e("Draw Points error", "error while drawing points on map");
        t.printStackTrace();
    }
        progDailog.dismiss();

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void callAsynchronousTask() {
    final Handler handler = new Handler();
    final Timer timer = new Timer();
    TimerTask doAsynchronousTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    try {
                        if(updateDeliveriesStatus == 1) {
                            timer.cancel();
                            updateDeliveriesStatus =0;
                        }
                        else {
                            updateDeliveriesSammary();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 5000  ms

}

    private void updateDeliveriesSammary() {
        if(checkNetworkConnection()) {
            new MapsDriver.StartDeliverySummaryHTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/UpdateDeliveryInfo");
        }
    }

    public void optionFloatingAction(View v){
        if(logout.getVisibility() == View.VISIBLE){
                logout.setVisibility(View.INVISIBLE);
                startDeliveries.setVisibility(View.INVISIBLE);
                engineOnOff.setVisibility(View.INVISIBLE);

        }else{

            logout.setVisibility(View.VISIBLE);
            startDeliveries.setVisibility(View.VISIBLE);
            engineOnOff.setVisibility(View.VISIBLE);
        }
    }
    public void logout(View v){
        editor.clear();
        editor.commit();
        startActivity(new Intent(this,ChoosePage.class));
    }
 public void engineOnOff(View v){
        if(engineRunning==1){
            engineOnOff.setText("Turn On Engine");
            engineRunning=0;
        }else{
            engineOnOff.setText("Turn Off Engine");
            engineRunning=1;
        }
    }

    public void AnswerDelivery(View v){
        if(checkNetworkConnection()){
            new MapsDriver.HTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/Deliveries/AnsweredDeliveries");
            engineRunning = 1;
        }

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
                if(jsonString.length() < 4){
                    Toast.makeText(MapsDriver.this,"no delivery available ",Toast.LENGTH_LONG).show();
                }else {
                    try {
                       /* jsonString = "[\n" +

                                "    {\n" +
                                "        \"id\": 14,\n" +
                                "        \"time\": \"2019-06-15T07:39:31.2174167\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 36.035482812499993,\n" +
                                "        \"sourceLatitude\": 33.683424674991556,\n" +
                                "        \"sourceCity\": \"Rif Dimashq\",\n" +
                                "        \"destinationLongtitude\": 38.276693749999993,\n" +
                                "        \"destinationLatitude\": 34.85459367701273,\n" +
                                "        \"destinationCity\": \"Homs\",\n" +
                                "        \"quantity\": 6,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 0\n" +
                                "    },\n" +
                                "    {\n" +
                                "        \"id\": 15,\n" +
                                "        \"time\": \"2019-06-15T07:40:26.8115965\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 37.463705468749993,\n" +
                                "        \"sourceLatitude\": 34.958205836965625,\n" +
                                "        \"sourceCity\": \"Homs\",\n" +
                                "        \"destinationLongtitude\": 35.420248437499993,\n" +
                                "        \"destinationLatitude\": 33.564498080450122,\n" +
                                "        \"destinationCity\": \"Sidon District\",\n" +
                                "        \"quantity\": 6,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 0\n" +
                                "    }\n" +
                                "]";*/
                   /*     jsonString = "[\n" +
                                "    {\n" +
                                "        \"id\": 172,\n" +
                                "        \"time\": \"2019-08-15T00:00:00\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 35.9199691,\n" +
                                "        \"sourceLatitude\": 34.4767160,\n" +
                                "        \"sourceCity\": \"Unnamed Road, Lebanon\",\n" +
                                "        \"destinationLongtitude\": 35.8865143,\n" +
                                "        \"destinationLatitude\": 34.4555092,\n" +
                                "        \"destinationCity\": \"Unnamed Road, Lebanon\",\n" +
                                "        \"quantity\": 58,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 20\n" +
                                "    },\n" +
                                "    {\n" +
                                "        \"id\": 28,\n" +
                                "        \"time\": \"2019-09-01T00:00:00\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 35.8711288,\n" +
                                "        \"sourceLatitude\": 34.4317324,\n" +
                                "        \"sourceCity\": \"Old Saida Rd, Lebanon\",\n" +
                                "        \"destinationLongtitude\": 35.8808763,\n" +
                                "        \"destinationLatitude\": 34.4124775,\n" +
                                "        \"destinationCity\": \"Unnamed Road, Lebanon\",\n" +
                                "        \"quantity\": 548,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 30\n" +
                                "    },\n" +
                                "    {\n" +
                                "        \"id\": 29,\n" +
                                "        \"time\": \"2019-09-15T00:00:00\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 35.8344791,\n" +
                                "        \"sourceLatitude\": 34.4182436,\n" +
                                "        \"sourceCity\": \"Old Saida Rd, Lebanon\",\n" +
                                "        \"destinationLongtitude\": 35.8406515,\n" +
                                "        \"destinationLatitude\": 34.3851495,\n" +
                                "        \"destinationCity\": \"Unnamed Road, Lebanon\",\n" +
                                "        \"quantity\": 96,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 40\n" +
                                "    },\n" +
                                "    {\n" +
                                "        \"id\": 30,\n" +
                                "        \"time\": \"2019-10-01T00:00:00\",\n" +
                                "        \"company\": null,\n" +
                                "        \"client\": null,\n" +
                                "        \"vehicle\": null,\n" +
                                "        \"driver\": null,\n" +
                                "        \"sourceLongtitude\": 35.7964879,\n" +
                                "        \"sourceLatitude\": 34.3602924,\n" +
                                "        \"sourceCity\": \"Old Saida Rd, Lebanon\",\n" +
                                "        \"destinationLongtitude\": 35.7349497,\n" +
                                "        \"destinationLatitude\": 34.3434136,\n" +
                                "        \"destinationCity\": \"Unnamed Road, Lebanon\",\n" +
                                "        \"quantity\": 3,\n" +
                                "        \"answered\": true,\n" +
                                "        \"started\": false,\n" +
                                "        \"finished\": false,\n" +
                                "        \"optimalDistance\": 0,\n" +
                                "        \"optimalTime\": 0,\n" +
                                "        \"optimalFuelConsumption\": 100\n" +
                                "    }\n" +
                                "]";

*/

                        JSONArray jr = new JSONArray(jsonString);
                        if(markerPoints.size() !=0) markerPoints.clear();
                        LatLng origin,dest;
                        for(int i =0 ;i<jr.length();i++){
                            JSONObject obj = (JSONObject)jr.getJSONObject(i);
                            deliveriesId.add(obj.getInt("id"));

                            markerPoints.add(new LatLng(obj.getDouble("sourceLatitude"), obj.getDouble("sourceLongtitude")));
                            markerPoints.add(new LatLng(obj.getDouble("destinationLatitude"), obj.getDouble("destinationLongtitude")));

                            int j = 2*i;
                            if(i==0){
                                MarkerOptions options = new MarkerOptions()
                                        .position(markerPoints.get(j))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                mMap.addMarker(options);

                            } else{
                                MarkerOptions options = new MarkerOptions()
                                                              .position(markerPoints.get(j));
                                mMap.addMarker(options);
                            }

                            MarkerOptions options2 = new MarkerOptions()
                                    .position(markerPoints.get(j+1));
                            mMap.addMarker(options2);

                             origin =markerPoints.get(j);
                             dest =markerPoints.get(j+1);

                            String url = getDirectionsUrl(origin, dest);

                            MapsDriver.DownloadTask downloadTask = new MapsDriver.DownloadTask();
                            downloadTask.execute(url);
                         }

                         for(int i = 0 ; i<jr.length()-1 ; i++){
                            int j = (2*i)+1;
                             origin = markerPoints.get(j);
                             dest = markerPoints.get(j+1);

                            String url = getDirectionsUrl(origin, dest);

                            MapsDriver.DownloadTask downloadTask = new MapsDriver.DownloadTask();
                            downloadTask.execute(url);
                        }


                         origin = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                         dest =markerPoints.get(0);

                        String url = getDirectionsUrl(origin, dest);

                        MapsDriver.DownloadTask1 downloadTask1 = new MapsDriver.DownloadTask1();
                        downloadTask1.execute(url);

                            } catch (Throwable t) {
                                Log.e("Driver Answered error", "Could not parse malformed JSON: \"" + jsonString + "\"");
                                t.printStackTrace();
                            }
                        }

            } else {
                Toast.makeText(MapsDriver.this,result,Toast.LENGTH_LONG).show();

            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            MapsDriver.ParserTask parserTask = new MapsDriver.ParserTask();
            //Invokes the thread for parsing the JSON data
            Log.d("result for parser Tast",result);
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                Log.d("first jasn object to the rout", jsonData[0]);
                DirectionsParser parser = new DirectionsParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    // lineOptions.width(2);
                    lineOptions.color(Color.RED);
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions!=null)
            mMap.addPolyline(lineOptions);

        }
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest;//+"&"+sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key=AIzaSyBSMoQ8TKnLJKYeto-_E5Y49-Oj7GkyhSQ"; //AIzaSyBSMoQ8TKnLJKYeto-_E5Y49-Oj7GkyhSQ
        return url;
    }

    private String HttpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        // 2. build JSON object
        JSONObject jsonObject = null;
            jsonObject = AnswerDeliveryJsonObject();
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
            Log.d("Response for answer deliveries: ", "> " + line);
        }

        return buffer.toString();
    }

    private JSONObject AnswerDeliveryJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("Username",prefs.getString("username",""));
        jsonObject.accumulate("Password",prefs.getString("password",""));

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
    /***********************************************************************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ){

            buildGoogleApiClient();

            mMap.setMyLocationEnabled(true);

        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(logout.getVisibility() == View.VISIBLE) {
                    if(logout.getVisibility() == View.VISIBLE){
                        logout.setVisibility(View.INVISIBLE);
                        startDeliveries.setVisibility(View.INVISIBLE);
                        engineOnOff.setVisibility(View.INVISIBLE);

                    }
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.getPosition().equals(markerPoints.get(0))){
                    if(markerPoints.size()%2 ==0 ){
                        //marker of source delivery
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsDriver.this);
                        alertDialog.setTitle("Start The Trip");
                        alertDialog.setPositiveButton("Conform", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder FuelAlert = new AlertDialog.Builder(MapsDriver.this);
                                FuelAlert.setTitle("Enter Fuel Level");
                                FuelAlert.setCancelable(false);
                                final EditText fuel = new EditText(MapsDriver.this);
                                fuel.setInputType(InputType.TYPE_CLASS_NUMBER);
                                FuelAlert.setView(fuel);

                                FuelAlert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_FuelLevel = fuel.getText().toString();
                                        AlertDialog.Builder OdometerAlert = new AlertDialog.Builder(MapsDriver.this);
                                        OdometerAlert.setTitle("Enter Odometer Value");
                                        OdometerAlert.setCancelable(false);

                                        final EditText odo = new EditText(MapsDriver.this);
                                        odo.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        OdometerAlert.setView(odo);

                                        OdometerAlert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                m_StartOdometer = odo.getText().toString();
                                                if(checkNetworkConnection()) {
                                                    new MapsDriver.StartDeliverySummaryHTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/StartDeliverySummary");
                                                }
                                                markerPoints.remove(0);
                                                hideSoftKeyboard();
                                                callAsynchronousTask();
                                                drawPoints();

                                            }
                                        });
                                        OdometerAlert.show();
                                    }
                                });
                                FuelAlert.show();
                                currentDateandTime = sdf.format(new Date());
                            }
                        });
                        alertDialog.create().show();
                }    else{
                        //marker of destination delivery
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsDriver.this);
                        alertDialog.setTitle("End The Trip");
                        alertDialog.setPositiveButton("Conform", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                AlertDialog.Builder FuelAlert = new AlertDialog.Builder(MapsDriver.this);
                                FuelAlert.setTitle("Enter Fuel Level");
                                FuelAlert.setCancelable(false);
                                final EditText fuel = new EditText(MapsDriver.this);
                                fuel.setInputType(InputType.TYPE_CLASS_NUMBER);
                                FuelAlert.setView(fuel);

                                FuelAlert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_FuelLevel = fuel.getText().toString();
                                        AlertDialog.Builder OdometerAlert = new AlertDialog.Builder(MapsDriver.this);
                                        OdometerAlert.setTitle("Enter Odometer Value");
                                        OdometerAlert.setCancelable(false);

                                        final EditText odo = new EditText(MapsDriver.this);
                                        odo.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        OdometerAlert.setView(odo);

                                        OdometerAlert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                m_StartOdometer = odo.getText().toString();
                                                if(checkNetworkConnection()) {
                                                    new MapsDriver.StartDeliverySummaryHTTPAsyncTask().execute("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/FinishDeliverySummary");
                                                }
                                                markerPoints.remove(0);
                                                deliveriesId.remove(0);
                                                updateDeliveriesStatus=1;
                                                hideSoftKeyboard();
                                                drawPoints();

                                            }
                                        });
                                        OdometerAlert.show();
                                    }
                                });

                                FuelAlert.show();

                                currentDateandTime = sdf.format(new Date());

                            }
                        });
                        alertDialog.create().show();

                    }
               }
                return false;
            }
        });

    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f, "current location");

                    } else {
                        Toast.makeText(MapsDriver.this, "unable to get your location", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    public  void moveCamera(LatLng latLng, float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        if(title.equals("current location")){  options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));  }
        else
        {
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    private void  hideSoftKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);

            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission denied!!",Toast.LENGTH_LONG);
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if(currentUserLocationMarker != null )
        {
            currentUserLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
       /* MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("current location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentUserLocationMarker = mMap.addMarker(markerOptions);*/

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

        if(googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(11000);
        locationRequest.setFastestInterval(11000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    /***************/
    public class StartDeliverySummaryHTTPAsyncTask extends AsyncTask<String, Void, String> {
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

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("OKStart")) {

                try {

                    JSONObject obj = new JSONObject(jsonStartDelivery);
                    currentDeliverySummatyId = obj.get("deliverySummaryId").toString();
                    Toast.makeText(MapsDriver.this,"summary started",Toast.LENGTH_LONG).show();
                } catch (Throwable t) {

                    Log.e("My App", "Could not parse malformed JSON: \"" + jsonStartDelivery + "\"");
                }
            }
            if (result.equals("OKFinish")){
                try {

                    JSONObject obj = new JSONObject(jsonFinishDelivery);
                     Toast.makeText(MapsDriver.this,"summary finished",Toast.LENGTH_LONG).show();
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + jsonFinishDelivery + "\"");
                }
            }
            if(result.equals("OKUpdate")){
                try {

                    JSONObject obj = new JSONObject(jsonUpdateDelivery);
                    Toast.makeText(MapsDriver.this,"update summary ",Toast.LENGTH_SHORT).show();
                    Log.d("update summary",  jsonUpdateDelivery + "\"");

                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + jsonUpdateDelivery + "\"");
                }
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
        JSONObject jsonObject = null;
        if(myUrl.equals("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/StartDeliverySummary")){
            jsonObject = StartDeliverySummaryJsonObject();
            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject);
            // 4. make POST request to the given URL
            conn.connect();
            // 5. return response message

            jsonStartDelivery = getJsonFile(conn);
            return conn.getResponseMessage()+"Start";
        }else{
            if(myUrl.equals("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/FinishDeliverySummary")){
                jsonObject = FinishDeliverySummaryJsonObject();
                // 3. add JSON content to POST request body
                setPostRequestContent(conn, jsonObject);
                // 4. make POST request to the given URL
                conn.connect();
                // 5. return response message

                jsonFinishDelivery = getJsonFile(conn);
                return conn.getResponseMessage()+"Finish";
            }else{
                if(myUrl.equals("http://kamalsmrsyd-001-site1.htempurl.com/api/DeliverySummaries/UpdateDeliveryInfo")){
                    jsonObject = UpdateDeliverySummaryJsonObject();
                    // 3. add JSON content to POST request body
                    setPostRequestContent(conn, jsonObject);
                    // 4. make POST request to the given URL
                    conn.connect();
                    // 5. return response message

                    jsonUpdateDelivery = getJsonFile(conn);
                    return conn.getResponseMessage()+"Update";
                }
            }
        }

        return  null;
    }

    private JSONObject UpdateDeliverySummaryJsonObject()throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.accumulate("DeliverySummaryId",currentDeliverySummatyId);
        jsonObject.accumulate("HarshAccelerationAndDeceleration",1);
        jsonObject.accumulate("HarshBreakings",1);
        jsonObject.accumulate("HardCornering",1);
        jsonObject.accumulate("Speedings",1);
        jsonObject.accumulate("SeatBelt",1);
        jsonObject.accumulate("OverRevving",1);
        jsonObject.accumulate("EngineRunning",engineRunning);
        jsonObject.accumulate("Latitude",lastLocation.getLatitude());
        jsonObject.accumulate("Longtitude",lastLocation.getLongitude());

        return jsonObject;
    }

    private JSONObject StartDeliverySummaryJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("DeliveryId",deliveriesId.get(0));
        jsonObject.accumulate("StartTime",currentDateandTime);
        jsonObject.accumulate("StartFuelLevel",m_FuelLevel);
        jsonObject.accumulate("StartOdometer",m_StartOdometer);
        jsonObject.accumulate("Latitude",lastLocation.getLatitude());
        jsonObject.accumulate("Longtitude",lastLocation.getLongitude());

        return jsonObject;
    }
    private JSONObject FinishDeliverySummaryJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("DeliverySummaryId",currentDeliverySummatyId);
        jsonObject.accumulate("EndTime",currentDateandTime);
        jsonObject.accumulate("EndFuelLevel",m_FuelLevel);
        jsonObject.accumulate("EndOdometer",m_StartOdometer);

        return jsonObject;
    }
    /****************888888*/
    private class DownloadTask1 extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            MapsDriver.ParserTask1 parserTask1 = new MapsDriver.ParserTask1();

            // Invokes the thread for parsing the JSON data
            parserTask1.execute(result);
        }
    }
    private class ParserTask1 extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsParser parser = new DirectionsParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions

                lineOptions.addAll(points);
                // lineOptions.width(2);
                lineOptions.color(Color.BLUE);

            }
            // Drawing polyline in the Google Map for the i-th route

            mMap.addPolyline(lineOptions);

        }
    }

}

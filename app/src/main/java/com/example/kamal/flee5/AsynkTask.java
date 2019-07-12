package com.example.kamal.flee5;

import android.os.AsyncTask;
import android.util.Log;

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

public class AsynkTask extends AsyncTask<String, Void, String> {
    public static String jsonString="";
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

        } else {

        }
    }



    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        //jsonObject.accumulate();

        return jsonObject;
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




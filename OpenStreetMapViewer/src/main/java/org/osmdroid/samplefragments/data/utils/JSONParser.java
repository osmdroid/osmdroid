package org.osmdroid.samplefragments.data.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Based an a SO answer, modified to meet needs
 * <p>
 * only suitable for small objects
 * http://stackoverflow.com/a/13196451/1203182
 *
 * @since 5.6.3
 */

public class JSONParser {


    // constructor
    public JSONParser() {

    }

    // function get json from url
// by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String url) throws IOException {

        InputStream is = null;
        JSONObject jObj = null;
        String json = null;
        // Making HTTP request
        try {

            is = new URL(url).openStream();


        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            json = sb.toString();
            reader.close();

        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}
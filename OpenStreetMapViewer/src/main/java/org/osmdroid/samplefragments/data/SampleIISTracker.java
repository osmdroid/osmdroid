package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;
import org.osmdroid.R;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.samplefragments.data.utils.JSONParser;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Calls a public rest endpoint for the current location of the IIS, but's an icon at that location
 * and centers the map at that location.
 * <p>
 * http://api.open-notify.org/iss-now.json
 * <p>
 * <p>
 * created on 1/6/2017.
 *
 * @author Alex O'Ree
 * @since 5.6.3
 */


public class SampleIISTracker extends SampleGridlines {

    final static String url_select = "http://api.open-notify.org/iss-now.json";

    boolean alive = true;
    Marker marker;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS yyyy-MMM-dd");

    JSONParser json=new JSONParser();
    NumberFormat nf = new DecimalFormat("###.#####");
    ConnectivityManager cm;

    @Override
    public String getSampleTitle() {
        return "Internal Space Station Tracker (Network connection required)";
    }

    boolean added=false;
    Timer t;
    TimerTask task;

    protected void addOverlays() {
        super.addOverlays();

        mMapView.setTilesScaledToDpi(true);
        mMapView.getController().setZoom(3);

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);


        marker = new Marker(mMapView);
        marker.setImage(getResources().getDrawable(R.drawable.sfppt));
        marker.setIcon(getResources().getDrawable(R.drawable.sfppt_small));
        marker.setTitle("International Space Station");
        task = new TimerTask() {
            @Override
            public void run() {
                final GeoPoint location = getLocation();
                if (location != null) {
                    Activity activity = getActivity();
                    if (activity != null) try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    marker.setPosition(location);
                                    mMapView.getController().setCenter(location);
                                    marker.setSnippet(nf.format(location.getLatitude()) + "," + nf.format(location.getLongitude()));
                                    if (marker.isInfoWindowShown()) {
                                        marker.closeInfoWindow();
                                        marker.showInfoWindow();
                                    }
                                    if (!added) {
                                        //only add it once
                                        mMapView.getOverlayManager().add(marker);
                                        added = true;
                                    }

                                } catch (Exception ex) {
                                    Log.e(TAG, "error updating marker", ex);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        Log.e(TAG, "error schedule task ", ex);
                    }
                }
            }
        };
        t = new Timer();
        t.schedule(task, 1000, 1000);

    }


    /**
     * HTTP callout to get a JSON document that represents the IIS's current location
     *
     * @return
     */
    private GeoPoint getLocation() {

        //sample data
        //{"timestamp": 1483742439, "iss_position": {"latitude": "-50.8416", "longitude": "-41.2701"}, "message": "success"}

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        GeoPoint pt = null;
        if (isConnected) {
            try {
                JSONObject jsonObject = json.makeHttpRequest(url_select);
                JSONObject iss_position = (JSONObject) jsonObject.get("iss_position");
                double lat = iss_position.getDouble("latitude");
                double lon = iss_position.getDouble("longitude");
                //valid the data
                if (lat <= 90d && lat >= -90d && lon >= -180d && lon <= 180d) {
                    pt = new GeoPoint(lat, lon);
                } else
                    Log.e(TAG, "invalid lat,lon received");
            } catch (Throwable e) {
                Log.e(TAG, "error fetching json", e);
            }
        }
        return pt;
    }

    @Override
    public void onPause() {
        super.onPause();
        alive = false;
        if (t != null)
            t.cancel();
        t = null;
    }

    public boolean skipOnCiTests() {
        return true;
    }

    @Override
    public void onDestroyView() {
        alive = false;
        if (t != null)
            t.cancel();
        t = null;
        marker.onDetach(mMapView);
        marker = null;
        super.onDestroyView();
    }



}

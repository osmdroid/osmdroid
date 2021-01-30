package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;
import org.osmdroid.R;
import org.osmdroid.samplefragments.data.utils.JSONParser;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * created on 1/7/2017.
 *
 * @author Alex O'Ree
 */

public abstract class IISTrackerBase extends SampleGridlines {

    final static String url_select = "http://api.open-notify.org/iss-now.json";

    boolean alive = true;
    Marker marker;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS yyyy-MMM-dd");

    JSONParser json = new JSONParser();
    NumberFormat nf = new DecimalFormat("###.#####");
    ConnectivityManager cm;

    abstract boolean isMotionTrail();

    boolean added = false;
    int motionTrailCounter = 0;
    Timer t;
    TimerTask task;
    Drawable icon;
    //Drawable icon_old;
    Drawable image;

    protected void addOverlays() {
        super.addOverlays();

        mMapView.setTilesScaledToDpi(true);
        mMapView.getController().setZoom(3);

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        image = getResources().getDrawable(R.drawable.sfppt);
        icon = getResources().getDrawable(R.drawable.sfppt_small);
        //icon_old=getResources().getDrawable(R.drawable.sfppt_small);
        //icon_old.setAlpha(77);

        marker = new Marker(mMapView);
        marker.setImage(image);
        marker.setIcon(icon);
        marker.setTitle("International Space Station");


    }

    public void onResume() {
        super.onResume();
        startTask();
    }

    private void startTask() {
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
                                    if (isMotionTrail()) {
                                        //motion trails on
                                        //only keep an icon on the map every 30 iterations
                                        //only keep a max of 500 icons on the map
                                        boolean wasOpen = false;
                                        if (marker != null && marker.isInfoWindowShown()) {
                                            marker.closeInfoWindow();
                                            wasOpen = true;
                                        }
                                        motionTrailCounter++;
                                        if (motionTrailCounter != 30) {
                                            //at 30 we keep the trail, otherwise remove it
                                            mMapView.getOverlayManager().remove(marker);
                                            marker.onDetach(mMapView);

                                        } else {
                                            //change the icon to something that makes it obvious that it's an old location
                                            marker.setAlpha(0.3f);
                                            motionTrailCounter = 0;
                                        }

                                        marker = new Marker(mMapView);
                                        marker.setImage(image);
                                        marker.setIcon(icon);
                                        marker.setTitle("International Space Station");
                                        marker.setPosition(location);
                                        mMapView.getController().setCenter(location);
                                        marker.setSnippet(nf.format(location.getLatitude()) + "," + nf.format(location.getLongitude()));
                                        //only add it once
                                        mMapView.getOverlayManager().add(marker);
                                        if (wasOpen)
                                            marker.showInfoWindow();
                                        if (mMapView.getOverlayManager().size() > 500) {
                                            Overlay overlay = mMapView.getOverlayManager().get(1);
                                            if (overlay instanceof Marker) {
                                                mMapView.getOverlayManager().remove(overlay);
                                                overlay.onDetach(mMapView);
                                                overlay = null;
                                            }
                                        }


                                    } else {
                                        //motion trails are disabled
                                        //basically, we only want 1 icon on the map for the space station
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

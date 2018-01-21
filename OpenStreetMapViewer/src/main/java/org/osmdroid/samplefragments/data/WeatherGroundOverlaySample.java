package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.GroundOverlay2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Sample on how to use the ground overlay, which places a geospatially referenced image on the map,
 * scaling with zoom.
 *
 * Related issues
 * <a href="https://github.com/osmdroid/osmdroid/issues/883">https://github.com/osmdroid/osmdroid/issues/883</a>
 * <a href="https://github.com/osmdroid/osmdroid/issues/684">https://github.com/osmdroid/osmdroid/issues/684</a>
 * created on 1/21/2018.
 *
 * @author Alex O'Ree
 */

public class WeatherGroundOverlaySample extends BaseSampleFragment implements Runnable{
    public static final String URL = "https://radar.weather.gov/Conus/RadarImg/latest_Small.gif";

    ConnectivityManager cm;
    GroundOverlay2 overlay;

    @Override
    public String getSampleTitle() {
        return "Live weather for USA using Ground Overlay";
    }

    @Override
    public void addOverlays(){
        super.addOverlays();

        overlay = new GroundOverlay2();
        overlay.setTransparency(0.5f);

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);


    }


    public void onResume(){
        super.onResume();
        new Thread(this).start();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (overlay!=null)
            mMapView.getOverlayManager().remove(overlay);

    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void run() {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            URLConnection con=null;
            InputStream is=null;
            try {
                URL  url = new URL(URL);
                con = url.openConnection();

                is = con.getInputStream();
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                overlay.setImage(bitmap);
                overlay.setPosition(new GeoPoint(50.0,-127.5), new GeoPoint( 21.0, -66.5));
                Activity act = getActivity();
                if (act!=null)
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMapView.getOverlayManager().add(overlay);
                        }
                    });
            } catch (Throwable e) {
                Log.e(TAG, "error fetching image", e);
            } finally {
                if (is!=null) try {
                    is.close();
                } catch (IOException e) {

                }

            }
        }
    }
}

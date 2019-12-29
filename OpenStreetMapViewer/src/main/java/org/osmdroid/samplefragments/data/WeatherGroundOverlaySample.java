package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.GroundOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample on how to use the ground mOverlay, which places a geospatially referenced image on the map,
 * scaling with zoom.
 * <p>
 * Related issues
 * <a href="https://github.com/osmdroid/osmdroid/issues/883">https://github.com/osmdroid/osmdroid/issues/883</a>
 * <a href="https://github.com/osmdroid/osmdroid/issues/684">https://github.com/osmdroid/osmdroid/issues/684</a>
 * created on 1/21/2018.
 *
 * @author Alex O'Ree
 */

public class WeatherGroundOverlaySample extends BaseSampleFragment implements Runnable {
    public static final String URL = "https://radar.weather.gov/Conus/RadarImg/latest_Small.gif";

    private final GeoPoint mNorthEast = new GeoPoint(50.0, -127.5);
    private final GeoPoint mSouthWest = new GeoPoint(21.0, -66.5);

    private ConnectivityManager cm;
    private GroundOverlay mOverlay;

    @Override
    public String getSampleTitle() {
        return "Live weather for USA using Ground Overlay";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();

        mOverlay = new GroundOverlay();
        mOverlay.setTransparency(0.5f);
        mOverlay.setPosition(mNorthEast, mSouthWest);
        mMapView.getOverlayManager().add(mOverlay);

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        mMapView.post(new Runnable() {
            @Override
            public void run() {
                final List<GeoPoint> geoPoints = new ArrayList<>();
                geoPoints.add(mNorthEast);
                geoPoints.add(mSouthWest);
                mMapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(geoPoints), false, 50);
            }
        });

        Toast.makeText(getActivity(), "Downloading the weather image...", Toast.LENGTH_SHORT).show();
        new Thread(this).start();
    }

    @Override
    public void run() {
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Activity act = getActivity();
            if (act != null) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Cannot connect!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return;
        }

        URLConnection con;
        InputStream is = null;
        try {
            final URL url = new URL(URL);
            con = url.openConnection();

            is = con.getInputStream();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            mOverlay.setImage(bitmap);
            final Activity act = getActivity();
            if (act != null) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Weather image downloaded!", Toast.LENGTH_SHORT).show();
                        mMapView.invalidate();
                    }
                });
            }
        } catch (Throwable e) {
            Toast.makeText(getActivity(), "Cannot download the weather image!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "error fetching image", e);
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                //
            }
        }
    }
}

package org.osmdroid.samplefragments.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import org.osmdroid.views.overlay.Overlay;
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


public class SampleIISTracker extends IISTrackerBase {

    @Override
    public String getSampleTitle() {
        return "Internal Space Station Tracker (Network connection required)";
    }

    @Override
    boolean isMotionTrail() {
        return false;
    }
}

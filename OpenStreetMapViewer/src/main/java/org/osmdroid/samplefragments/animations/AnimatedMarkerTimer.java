package org.osmdroid.samplefragments.animations;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Demonstrates a one way to move an icon in an animation.
 * It's dirty but it works
 * created on 7/29/2017.
 * https://github.com/osmdroid/osmdroid/issues/636
 *
 * @author Alex O'Ree
 */

public class AnimatedMarkerTimer extends BaseSampleFragment implements MapListener {

    boolean alive = true;
    FolderOverlay activeLatLonGrid;
    Marker marker;
    Timer t;
    TimerTask task;
    boolean added=false;
    @Override
    public String getSampleTitle() {
        return "Animated Marker";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        LatLonGridlineOverlay.setDefaults();
        mMapView.getController().setCenter(new GeoPoint(0d, 0d));
        mMapView.getController().setZoom(5);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setMapListener(this);
        mMapView.getController().setZoom(3);

        marker = new Marker(mMapView);
        marker.setPosition(new GeoPoint(45d, -74d));

        updateGridlines();

    }



    @Override
    public boolean onScroll(ScrollEvent scrollEvent) {
        updateGridlines();
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent zoomEvent) {
        updateGridlines();
        return false;
    }


    protected void updateGridlines() {

        if (mMapView == null)
            return; //happens during unit tests with rapid recycling of the fragment
        if (activeLatLonGrid != null) {
            mMapView.getOverlayManager().remove(activeLatLonGrid);
            activeLatLonGrid.onDetach(mMapView);
        }
        LatLonGridlineOverlay.backgroundColor = Color.BLACK;
        LatLonGridlineOverlay.fontColor = Color.GREEN;
        LatLonGridlineOverlay.lineColor = Color.GREEN;
        activeLatLonGrid = LatLonGridlineOverlay.getLatLonGrid(getActivity(), mMapView);
        mMapView.getOverlays().add(activeLatLonGrid);

    }

    public void onResume(){
        super.onResume();
        startTask();
    }


    private void startTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                GeoPoint current = marker.getPosition();
                if (current==null)
                    current = new GeoPoint(45d, -74d);
                final GeoPoint location = new GeoPoint(current.getLatitude(), current.getLongitude()+0.0003);
                if (location != null) {
                    Activity activity = getActivity();
                    if (activity != null) try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                        marker.setPosition(location);
                                        mMapView.getController().setCenter(location);

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

package org.osmdroid.samplefragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.osmdroid.R;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.IconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

/**
 * #394
 *
 * Created by k3b on 01.09.2016.
 */
public class AsyncTaskDemoFragment extends BaseSampleFragment {
    public static final String TAG = "osmAsync";
	/** If there is more than 200 millisecs no zoom/scroll update markers */
	protected static final int DEFAULT_INACTIVITY_DELAY_IN_MILLISECS = 200;

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String TITLE = "AsyncTaskDemoFragment - Load Icons in AsyncTask";

    private static final int MENU_ZOOMIN_ID = Menu.FIRST;
    private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
    private static final int MENU_LAST_ID = MENU_ZOOMIN_ID + 1; // Always set to last unused id

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.setTileSource(TileSourceFactory.MAPNIK);
		
		// If there is more than 200 millisecs no zoom/scroll update markers
        mMapView.setMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                reloadMarker();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                reloadMarker();
                return false;
            }
        }, DEFAULT_INACTIVITY_DELAY_IN_MILLISECS));

        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);

        final Context context = getActivity();
        icon = context.getResources().getDrawable(R.drawable.person);
        currentBackgroundContentFolder = new FolderOverlay();

        mMapView.getOverlays().add(currentBackgroundContentFolder);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Put overlay items first
        mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);

        menu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
        menu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView)) {
            return true;
        }

        switch (item.getItemId()) {
            case MENU_ZOOMIN_ID:
                mMapView.getController().zoomIn();
                return true;

            case MENU_ZOOMOUT_ID:
                mMapView.getController().zoomOut();
                return true;
        }
        return false;
    }

    //---------------------------------------------------------------
    /** Load ItemizedIconOverlay in a Background Task.
     this allows canceling of loading task. There are 0 or one tasks running at a time */
    private AsyncTask<Double, Integer, FolderOverlay> currentBackgroundMarkerLoaderTask = null;

    private Drawable icon = null;
    private FolderOverlay currentBackgroundContentFolder = null;

    private void reloadMarker() {
        // initialized
        if (currentBackgroundMarkerLoaderTask == null) {
            // not active yet
            // List<Overlay> oldItems = mFolderOverlaySummaryMarker.getItems();

            int zoom = this.mMapView.getZoomLevel();
            BoundingBox world = this.mMapView.getBoundingBox();

            reloadMarker(world, zoom);
        }
    }

    private void reloadMarker(BoundingBox latLonArea, int zoom) {
        Log.d(TAG,"reloadMarker " + latLonArea + ", zoom " + zoom);
        this.currentBackgroundMarkerLoaderTask = new BackgroundMarkerLoaderTask();
        this.currentBackgroundMarkerLoaderTask.execute(
                latLonArea.getLatSouth(), latLonArea.getLatNorth(),
                latLonArea.getLonEast(), latLonArea.getLonWest(), (double) zoom);
    }

    /** */
    private class BackgroundMarkerLoaderTask extends AsyncTask<Double, Integer, FolderOverlay> {

        /**
         * Computation of the map itmes in the non-gui background thread. .
         *
         * @param params latMin, latMax, lonMin, longMax, zoom.
         * @return A new FolderOverlay that contain map data for latMin, latMax, lonMin, longMax, zoom.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected FolderOverlay doInBackground(Double... params) {
            FolderOverlay result = new FolderOverlay();

            try {
                if (params.length != 5) throw new IllegalArgumentException("expected latMin, latMax, lonMin, longMax, zoom");

                int paramNo = 0;
                double latMin = params[paramNo++];
                double latMax = params[paramNo++];
                double lonMin = params[paramNo++];
                double lonMax = params[paramNo++];

                if (latMin > latMax) {
                    double t = latMax;
                    latMax = latMin;
                    latMin = t;
                }

                if (lonMin > lonMax) {
                    double t = lonMax;
                    lonMax = lonMin;
                    lonMin = t;
                }
                int zoom = params[paramNo++].intValue();

                Log.d(TAG,"async doInBackground" +
                        " latMin=" +latMin+
                        " ,latMax=" +latMax+
                        " ,lonMin=" +lonMin+
                        " ,lonMax=" +lonMax+
                        ", zoom=" + zoom);
                // simulate heavy computation ...
                Thread.sleep(1000, 0);

                // i.e.
                // SELECT poi.lat, poi.lon, poi.id, poi.name FROM poi
                //    WHERE poi.lat >= {latMin} AND poi.lat <= {latMax}
                //          AND poi.lon >= {lonMin} AND poi.lon <= {lonMax}
                //          AND {zoom} >= poi.zoomMin AND {zoom} <= poi.zoomMax

                double latStep = Math.abs(latMax-latMin) / 6;
                double lonStep = Math.abs(lonMax-lonMin) / 6;
                for (double lat = latMin; lat <= latMax; lat += latStep) {
                    for (double lon = lonMin; lon <= lonMax; lon += lonStep) {
                        result.add(createMarker(lat, lon, zoom));
                    }

                }
            } catch (Exception ex) {
                // TODO more specific error handling
                Log.e(TAG,"doInBackground  " + ex.getMessage(),ex);
                cancel(false);
            }

            if (!isCancelled()) {
                Log.d(TAG,"doInBackground result " + result.getItems().size());
                return result;
            }
            Log.d(TAG,"doInBackground cancelled");
            return null;
        }

        // This is called in gui-thread when doInBackground() is finished.
        @Override
        protected void onPostExecute(FolderOverlay result) {
            if (!isCancelled() && (result != null)) {
                showMarker(result);
                if (currentBackgroundMarkerLoaderTask == this) {
                    currentBackgroundMarkerLoaderTask = null;
                }
            }
        }
    }

    private Overlay createMarker(double lat, double lon, int zoom) {
        return new IconOverlay(new GeoPoint(lat,lon), icon);
//        return new IconOverlay(null, new GeoPoint(lat,lon), icon);
    }

    private void showMarker(FolderOverlay newMarker) {
        boolean modified = false;
        if (this.currentBackgroundContentFolder != null) {
            Log.d(TAG,"showMarker remove old " + this.currentBackgroundContentFolder.getItems().size());
            this.mMapView.getOverlays().remove(this.currentBackgroundContentFolder);
            this.currentBackgroundContentFolder.onDetach(mMapView);
            this.currentBackgroundContentFolder = null;
            modified = true;
        }

        if (newMarker != null) {
            this.currentBackgroundContentFolder = newMarker;
            Log.d(TAG,"showMarker add new " + this.currentBackgroundContentFolder.getItems().size() + ", isAnimating=" + mMapView.isAnimating());
            mMapView.getOverlays().add(newMarker);
            modified = true;
        }

        if (modified) {
            if (mMapView.isAnimating()) {
                mMapView.postInvalidate();
            } else {
                mMapView.invalidate();
            }
        }
    }

}

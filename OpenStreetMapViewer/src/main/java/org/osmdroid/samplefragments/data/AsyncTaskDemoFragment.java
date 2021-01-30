package org.osmdroid.samplefragments.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.osmdroid.R;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.IconOverlay;
import org.osmdroid.views.overlay.Overlay;

/**
 * #394 #398 Demonstration how to load/update markers from
 * Async Background task.
 * <p>
 * Created by k3b on 01.09.2016.
 */
public class AsyncTaskDemoFragment extends BaseSampleFragment {
    public static final String TAG = "osmAsync";
    /**
     * If there is more than 200 millisecs no zoom/scroll update markers
     */
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

        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);

        final Context context = getActivity();
        mMarkerIcon = context.getResources().getDrawable(R.drawable.person);
        mCurrentBackgroundContentFolder = new FolderOverlay();

        mMapView.getOverlays().add(mCurrentBackgroundContentFolder);

        setHasOptionsMenu(true);

        // MapView.OnFirstLayoutListener initial map display also triggers onScroll to update the markers
        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                mMapView.zoomToBoundingBox(new BoundingBox(56.0, 7.0, 45.0, 16.0), false);
            }
        });
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
    /**
     * Load {@link FolderOverlay} with {@link IconOverlay}s in a Background Task {@link BackgroundMarkerLoaderTask}.
     * mCurrentBackgroundMarkerLoaderTask.cancel() allows aboarding the loading task on screen rotation.
     * There are 0 or one tasks running at a time.
     */
    private BackgroundMarkerLoaderTask mCurrentBackgroundMarkerLoaderTask = null;

    /**
     * implementation detail: mMarkerIcon attached to each generated {@link IconOverlay}
     */
    private Drawable mMarkerIcon = null;

    /**
     * This must be reomoved from {@link org.osmdroid.views.MapView} when
     * {@link BackgroundMarkerLoaderTask} finishes
     */
    private FolderOverlay mCurrentBackgroundContentFolder = null;

    /**
     * if > 0 there where zoom/scroll events while {@link BackgroundMarkerLoaderTask} was active so
     * {@link #reloadMarker()} bust be called again.
     */
    private int mMissedMapZoomScrollUpdates = 0;

    /**
     * called by {@link org.osmdroid.views.MapView} if zoom or scroll has changed to
     * reload marker for new visible region in the {@link org.osmdroid.views.MapView}
     */
    private void reloadMarker() {
        // initialized
        if (mCurrentBackgroundMarkerLoaderTask == null) {
            // start background load
            double zoom = this.mMapView.getZoomLevelDouble();
            BoundingBox world = this.mMapView.getBoundingBox();

            reloadMarker(world, zoom);
        } else {
            // background load is already active. Remember that at least one scroll/zoom was missing
            mMissedMapZoomScrollUpdates++;
        }
    }

    /**
     * called by MapView if zoom or scroll has changed to reload marker for new visible region
     */
    private void reloadMarker(BoundingBox latLonArea, double zoom) {
        Log.d(TAG, "reloadMarker " + latLonArea + ", zoom " + zoom);
        this.mCurrentBackgroundMarkerLoaderTask = new BackgroundMarkerLoaderTask();
        this.mCurrentBackgroundMarkerLoaderTask.execute(
                latLonArea.getLatSouth(), latLonArea.getLatNorth(),
                latLonArea.getLonEast(), latLonArea.getLonWest(), zoom);
    }

    /**
     * Implements load {@link FolderOverlay} with {@link IconOverlay}s in a Background Task.
     */
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
                if (latMax - latMin < 0.00001)
                    return null;
                //this is a problem, abort https://github.com/osmdroid/osmdroid/issues/521

                if (lonMin > lonMax) {
                    double t = lonMax;
                    lonMax = lonMin;
                    lonMin = t;
                }
                int zoom = params[paramNo++].intValue();

                Log.d(TAG, "async doInBackground" +
                        " latMin=" + latMin +
                        " ,latMax=" + latMax +
                        " ,lonMin=" + lonMin +
                        " ,lonMax=" + lonMax +
                        ", zoom=" + zoom);
                // simulate heavy computation ...
                if (isCancelled())
                    return null;
                Thread.sleep(1000, 0);
                if (isCancelled())
                    return null;

                // i.e.
                // SELECT poi.lat, poi.lon, poi.id, poi.name FROM poi
                //    WHERE poi.lat >= {latMin} AND poi.lat <= {latMax}
                //          AND poi.lon >= {lonMin} AND poi.lon <= {lonMax}
                //          AND {zoom} >= poi.zoomMin AND {zoom} <= poi.zoomMax

                double latStep = Math.abs(latMax - latMin) / 6;
                double lonStep = Math.abs(lonMax - lonMin) / 6;
                for (double lat = latMin; lat <= latMax; lat += latStep) {
                    for (double lon = lonMin; lon <= lonMax; lon += lonStep) {
                        result.add(createMarker(lat, lon, zoom));
                        if (isCancelled())
                            break;
                    }
                    if (isCancelled())
                        break;

                }
            } catch (Exception ex) {
                // TODO more specific error handling
                Log.e(TAG, "doInBackground  " + ex.getMessage(), ex);
                cancel(false);
            }

            if (!isCancelled()) {
                Log.d(TAG, "doInBackground result " + result.getItems().size());
                return result;
            }
            Log.d(TAG, "doInBackground cancelled");
            return null;
        }

        // This is called in gui-thread when doInBackground() is finished.
        @Override
        protected void onPostExecute(FolderOverlay result) {
            if (!isCancelled() && (result != null)) {
                showMarker(result);
            }
            mCurrentBackgroundMarkerLoaderTask = null;
            // there was map move/zoom while {@link BackgroundMarkerLoaderTask} was active. must reload
            if (mMissedMapZoomScrollUpdates > 0) {
                Log.d(TAG, "onPostExecute: lost  " + mMissedMapZoomScrollUpdates + " MapZoomScrollUpdates. Reload items.");
                mMissedMapZoomScrollUpdates = 0;
                reloadMarker();
            }
        }
    }

    private Overlay createMarker(double lat, double lon, int zoom) {
        return new IconOverlay(new GeoPoint(lat, lon), mMarkerIcon);
    }

    /**
     * called in gui thread by {@link BackgroundMarkerLoaderTask} after loading has finished.
     */
    private void showMarker(FolderOverlay newMarker) {
        boolean modified = false;
        if (this.mCurrentBackgroundContentFolder != null) {
            Log.d(TAG, "showMarker remove old " + this.mCurrentBackgroundContentFolder.getItems().size());
            this.mMapView.getOverlays().remove(this.mCurrentBackgroundContentFolder);
            this.mCurrentBackgroundContentFolder.onDetach(mMapView);
            this.mCurrentBackgroundContentFolder = null;
            modified = true;
        }

        if (newMarker != null) {
            this.mCurrentBackgroundContentFolder = newMarker;
            Log.d(TAG, "showMarker add new " + this.mCurrentBackgroundContentFolder.getItems().size() + ", isAnimating=" + mMapView.isAnimating());
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

    @Override
    public void onDestroyView() {
        // called i.e. for screen rotation
        super.onDestroyView();
        if (mCurrentBackgroundMarkerLoaderTask != null) {
            // make shure that running {@link BackgroundMarkerLoaderTask} does not try to
            // update destroyed gui when finished
            mCurrentBackgroundMarkerLoaderTask.cancel(false);
            mCurrentBackgroundMarkerLoaderTask = null;
        }
        super.onDestroy();
    }
}

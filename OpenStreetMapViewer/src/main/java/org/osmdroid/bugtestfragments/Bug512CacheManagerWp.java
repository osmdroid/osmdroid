package org.osmdroid.bugtestfragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.osmdroid.R;
import org.osmdroid.api.IMapView;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

/**
 * https://github.com/osmdroid/osmdroid/issues/512#issuecomment-271219842
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class Bug512CacheManagerWp extends BaseSampleFragment implements CacheManager.CacheManagerCallback, View.OnClickListener {
    Button btnCache;
    @Override
    public String getSampleTitle() {
        return "Issue 512 Cache download using waypoints";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container,false);

        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Run job (watch logcat output)");

        // Workaround for failing unit test due to issues with https connections on API < 9
        // https://github.com/osmdroid/osmdroid/issues/1048
        // https://github.com/osmdroid/osmdroid/issues/1051
        // Also works around an issue with some emulator image that do not update their time/date
        // correctly and stay on 0 unix time. This causes SSLExceptions due to the validity of the
        // certificates.
        OnlineTileSourceBase onlineTileSourceBase = new XYTileSource("Mapnik",
                    0, 19, 256, ".png",
                    new String[]{
                            "http://a.tile.openstreetmap.org/",
                            "http://b.tile.openstreetmap.org/",
                            "http://c.tile.openstreetmap.org/"}, "© OpenStreetMap contributors");

        mMapView = new MapView(getActivity(), new MapTileProviderBasic(
                getActivity().getApplicationContext(), onlineTileSourceBase));

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);

        return root;
    }

    CacheManager.CacheManagerTask downloadingTask=null;

    @Override
    public boolean skipOnCiTests(){
        return false;
    }

    @Override
    public void runTestProcedures() throws Exception{
        final CacheManager mgr = new CacheManager(mMapView);
        final ArrayList<GeoPoint> pts= new ArrayList<>();
        pts.add(new GeoPoint(38.89775, -77.03690));
        pts.add(new GeoPoint(38.87101, -77.05641));
        taskRunning=true;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                downloadingTask = mgr.downloadAreaAsyncNoUI(mMapView.getContext(), pts, 0, 4, Bug512CacheManagerWp.this);
            }
        });
        //downloadingTask = mgr.downloadAreaAsync(mMapView.getContext(), pts, 0, 5, this);
        int timeoutSeconds=30;
        while (taskRunning && timeoutSeconds>0) {
            Thread.sleep(1000);
            timeoutSeconds--;
        }
        if (!taskRunning){
            //great we're done
            if (success) {
                //test passed
                return;
            } else {
                throw new RuntimeException("Failure occurred during the test, there were " + errors);
            }
        }

    }
    boolean taskRunning=false;
    boolean success=false;
    int errors=0;

    @Override
    public void onTaskComplete() {
        Log.i(IMapView.LOGTAG, "download job complete no errors");
        taskRunning=true;
        success=true;
    }

    @Override
    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
        Log.i(IMapView.LOGTAG, "download update : " + progress + " " + currentZoomLevel + " " + zoomMin + " " + zoomMax);
    }

    @Override
    public void downloadStarted() {

        Log.i(IMapView.LOGTAG, "download job started");
    }

    @Override
    public void setPossibleTilesInArea(int total) {

        Log.i(IMapView.LOGTAG, "tiles to download " + total);
    }

    @Override
    public void onTaskFailed(int errors) {
        this.errors=errors;
        Log.i(IMapView.LOGTAG, "down job failed with error count: " + errors);
        taskRunning=false;
    }

    @Override
    public void onClick(View v) {

        try {
            runTestProcedures();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

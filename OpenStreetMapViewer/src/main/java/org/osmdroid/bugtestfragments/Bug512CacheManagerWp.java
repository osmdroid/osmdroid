package org.osmdroid.bugtestfragments;

import android.os.AsyncTask;

import junit.framework.Assert;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class Bug512CacheManagerWp extends BaseSampleFragment implements CacheManager.CacheManagerCallback {
    @Override
    public String getSampleTitle() {
        return "Issue 512 Cache download using waypoints";
    }


    CacheManager.DownloadingTask downloadingTask=null;
    /**
     * optional place to put automated test procedures, used during the connectCheck tests
     * this is called OFF of the UI thread. block this method call util the test is done
     */

    public void runTestProcedures() throws Exception{
        final CacheManager mgr = new CacheManager(mMapView);
        final ArrayList<GeoPoint> pts= new ArrayList<>();
        pts.add(new GeoPoint(38.89775, -77.03690));
        pts.add(new GeoPoint(38.87101, -77.05641));
        taskRunning=true;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                downloadingTask = mgr.downloadAreaAsync(mMapView.getContext(), pts, 0, 5, Bug512CacheManagerWp.this);
            }
        });
        //downloadingTask = mgr.downloadAreaAsync(mMapView.getContext(), pts, 0, 5, this);
        int timeoutSeconds=300;
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
                Assert.fail("Failure occurred during the test, there were " + errors);
            }
        }

    }
    boolean taskRunning=false;
    boolean success=false;
    int errors=0;

    @Override
    public void onTaskComplete() {
        taskRunning=true;
        success=true;
    }

    @Override
    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {

    }

    @Override
    public void downloadStarted() {

    }

    @Override
    public void setPossibleTilesInArea(int total) {

    }

    @Override
    public void onTaskFailed(int errors) {
        this.errors=errors;
    }
}

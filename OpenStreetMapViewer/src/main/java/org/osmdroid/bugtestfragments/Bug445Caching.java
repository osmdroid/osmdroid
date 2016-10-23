package org.osmdroid.bugtestfragments;

import android.util.Log;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.util.GeoPoint;

/**
 * Created by alex on 10/21/16.
 */

public class Bug445Caching  extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Bug 445 Ensure Caching works";
    }

    SqlTileWriter writer=null;
    protected void addOverlays() {
        IFilesystemCache tileWriter = mMapView.getTileProvider().getTileWriter();

        if (tileWriter instanceof SqlTileWriter){
            writer = (SqlTileWriter) tileWriter;
            writer.purgeCache();

        }
        mMapView.getController().setCenter(new GeoPoint(52.2742, 0.21130));
        mMapView.getController().setZoom(17);
    }

    public void runTestProcedures() throws Exception {
        if (writer==null)
            return;
        mMapView.setUseDataConnection(true);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(1);
            }
        });

        writer.purgeCache();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(17);
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(18);
            }
        });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(19);
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long count=writer.getRowCount(mMapView.getTileProvider().getTileSource().name());
        Log.i(TAG, "downloaded " + count + " tiles during the test");
        if (count< 20)  //it should be around 45 for the complete set, depending on screen size
            throw new Exception("only fetched " + count + " tiles");


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(1);
            }
        });

        mMapView.setUseDataConnection(false);
        mMapView.getTileProvider().clearTileCache();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(17);
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(18);
            }
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(19);
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mMapView.getTileProvider().getQueueSize()>0){
            throw new Exception("queue size is greater than expected " + mMapView.getTileProvider().getQueueSize());
        }


    }


}

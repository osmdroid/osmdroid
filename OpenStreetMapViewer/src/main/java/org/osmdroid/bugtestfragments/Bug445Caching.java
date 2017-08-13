package org.osmdroid.bugtestfragments;

import android.util.Log;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;

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

        final int minExpected = getMinTileExpected();

        writer.purgeCache();
        final long count = getDbCount();
        if (count != 0)
            throw new Exception("purge should remove all tiles, but " + count + " were found");

        checkDownload(17, minExpected);
        checkDownload(18, minExpected);
        checkDownload(19, minExpected);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(1);
            }
        });

        mMapView.setUseDataConnection(false);
        mMapView.getTileProvider().clearTileCache();

        checkCache(17);
        checkCache(18);
        checkCache(19);
    }

    /**
     * @since 6.0
     */
    private void checkDownload(final int pZoomLevel, final int pMinExpected) throws Exception{
        final long countBefore = getDbCount();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(pZoomLevel);
            }
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long countAfter = getDbCount();
        final long count = countAfter - countBefore;
        if (count < pMinExpected) {
            throw new Exception(
                    "only fetched " + count + " tiles"
                            + " for zoom level " + pZoomLevel
                            + " but " + pMinExpected + " were expected");
        }
        Log.i(TAG, "checkDownload ok for zoom level " + pZoomLevel);
    }

    /**
     * @since 6.0
     */
    private void checkCache(final int pZoomLevel) throws Exception{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(pZoomLevel);
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long queueSize = mMapView.getTileProvider().getQueueSize();
        if (queueSize > 0){
            throw new Exception(
                    "queue size is greater than expected: " + queueSize
                            + " for zoom level " + pZoomLevel);
        }
        Log.i(TAG, "checkCache ok for zoom level " + pZoomLevel);
    }

    /**
     * @since 6.0
     */
    private int getMinTileExpected() {
        final int width = mMapView.getWidth();
        Log.i(TAG, "width " + width);
        final int height = mMapView.getHeight();
        Log.i(TAG, "height " + height);
        final int tileSize = TileSystem.getTileSize();
        Log.i(TAG, "tile size " + tileSize);
        final int minCols = width / tileSize + (width % tileSize == 0 ? 0 : 1);
        Log.i(TAG, "min cols " + minCols);
        final int minRows = height / tileSize + (height % tileSize == 0 ? 0 : 1);
        Log.i(TAG, "min rows " + minRows);
        final int minExpected = minCols * minRows;
        Log.i(TAG, "min expected " + minExpected);
        return minExpected;
    }

    /**
     * @since 6.0
     */
    private long getDbCount() {
        final long count=writer.getRowCount(mMapView.getTileProvider().getTileSource().name());
        Log.i(TAG, "downloaded " + count + " tiles so far");
        return count;
    }
}

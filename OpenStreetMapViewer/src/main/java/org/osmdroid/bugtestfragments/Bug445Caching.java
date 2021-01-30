package org.osmdroid.bugtestfragments;

import android.util.Log;
import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;

/**
 * Created by alex on 10/21/16.
 */

public class Bug445Caching extends BaseSampleFragment {

    private static final GeoPoint center = new GeoPoint(52.2742, 0.21130);
    private static final int minZoom = 10; // should be high enough so that download is needed (cf. archive)
    private static final int maxZoom = 16; // should be 16 or lower due to osm tile server policy (there is no systematic cache of tiles on server for zoom 17+)
    private static final int initialZoom = minZoom - 1;

    @Override
    public String getSampleTitle() {
        return "Bug 445 Ensure Caching works";
    }

    SqlTileWriter writer = null;

    protected void addOverlays() {
        IFilesystemCache tileWriter = mMapView.getTileProvider().getTileWriter();

        if (tileWriter instanceof SqlTileWriter) {
            writer = (SqlTileWriter) tileWriter;
            writer.purgeCache();

        }
        setZoomAndCenter(initialZoom);
    }

    @Override
    public boolean skipOnCiTests() {
        return true;
    }

    @Override
    public void runTestProcedures() throws Exception {
        if (writer == null)
            return;
        mMapView.setUseDataConnection(true);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "downloading from zoom level " + minZoom + " to " + maxZoom, Toast.LENGTH_SHORT).show();
                setZoomAndCenter(initialZoom);
            }
        });

        writer.purgeCache();
        final long count = getDbCount();
        if (count != 0)
            throw new Exception("purge should remove all tiles, but " + count + " were found");

        int maxTilesNeeded = 0;
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            maxTilesNeeded += getMaxTileExpected(zoom);
        }
        mMapView.getTileProvider().ensureCapacity(maxTilesNeeded);

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            checkDownload(zoom);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "testing cache from zoom level " + minZoom + " to " + maxZoom, Toast.LENGTH_SHORT).show();
                setZoomAndCenter(initialZoom);
            }
        });

        mMapView.setUseDataConnection(false);

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            checkCache(zoom);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @since 6.0.0
     */
    private void checkDownload(final int pZoomLevel) throws Exception {
        final long countBefore = getDbCount();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "checking download for zoom level " + pZoomLevel, Toast.LENGTH_SHORT).show();
                setZoomAndCenter(pZoomLevel);
            }
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long countAfter = getDbCount();
        final long count = countAfter - countBefore;
        final int minExpected = getMinTileExpected(pZoomLevel);
        if (count < minExpected) {
            throw new Exception(
                    "only fetched " + count + " tiles"
                            + " for zoom level " + pZoomLevel
                            + " but " + minExpected + " were expected");
        }
        Log.i(TAG, "checkDownload ok for zoom level " + pZoomLevel);
    }

    /**
     * @since 6.0.0
     */
    private void checkCache(final int pZoomLevel) throws Exception {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setZoomAndCenter(pZoomLevel);
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long queueSize = mMapView.getTileProvider().getQueueSize();
        if (queueSize > 0) {
            throw new Exception(
                    "queue size is greater than expected: " + queueSize
                            + " for zoom level " + pZoomLevel);
        }
        Log.i(TAG, "checkCache ok for zoom level " + pZoomLevel);
    }

    /**
     * @since 6.0.0
     */
    private int getMinTileExpected(final int pZoomLevel) {
        final int maxPerZoom = 1 << pZoomLevel;
        Log.i(TAG, "max per zoom " + maxPerZoom);
        final int width = mMapView.getWidth();
        Log.i(TAG, "width " + width);
        final int height = mMapView.getHeight();
        Log.i(TAG, "height " + height);
        final int tileSize = TileSystem.getTileSize();
        Log.i(TAG, "tile size " + tileSize);
        final int minCols = getMinNumberExpected(tileSize, width, maxPerZoom);
        Log.i(TAG, "min cols " + minCols);
        final int minRows = getMinNumberExpected(tileSize, height, maxPerZoom);
        Log.i(TAG, "min rows " + minRows);
        final int minExpected = minCols * minRows;
        Log.i(TAG, "min expected " + minExpected);
        return minExpected;
    }

    /**
     * @since 6.0.0
     */
    private int getMaxTileExpected(final int pZoomLevel) {
        final int maxPerZoom = 1 << pZoomLevel;
        final int width = mMapView.getWidth();
        final int height = mMapView.getHeight();
        final int tileSize = TileSystem.getTileSize();
        final int minCols = getMaxNumberExpected(tileSize, width, maxPerZoom);
        final int minRows = getMaxNumberExpected(tileSize, height, maxPerZoom);
        return minCols * minRows;
    }

    /**
     * @since 6.0.0
     */
    private int getMinNumberExpected(final int pTileSize, final int pScreenSize, final int pMaxPerZoom) {
        return Math.min(pMaxPerZoom, pScreenSize / pTileSize + (pScreenSize % pTileSize == 0 ? 0 : 1));
    }

    /**
     * @since 6.0.0
     */
    private int getMaxNumberExpected(final int pTileSize, final int pScreenSize, final int pMaxPerZoom) {
        return Math.min(pMaxPerZoom, 1 + getMinNumberExpected(pTileSize, pScreenSize, pMaxPerZoom));
    }

    /**
     * @since 6.0.0
     */
    private long getDbCount() {
        final long count = writer.getRowCount(mMapView.getTileProvider().getTileSource().name());
        Log.i(TAG, "downloaded " + count + " tiles so far");
        return count;
    }

    /**
     * @since 6.0.0
     */
    private void setZoomAndCenter(final int pZoomLevel) {
        mMapView.getController().setZoom(pZoomLevel);
        mMapView.getController().setCenter(center);
        mMapView.invalidate();
    }
}

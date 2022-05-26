/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.junit.Test;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Neil Boyd
 */
public class MapTileProviderTest {


    private final List<Long> mTiles = new LinkedList<>();
    private final List<MapTileModuleProviderBase> mProviders = new ArrayList<>();

    private final IMapTileProviderCallback mTileProviderCallback = new IMapTileProviderCallback() {

        @Override
        public void mapTileRequestCompleted(final MapTileRequestState aState,
                                            final Drawable aDrawable) {
            mTiles.add(aState.getMapTile());
        }

        @Override
        public void mapTileRequestFailed(final MapTileRequestState aState) {
        }

        @Override
        public void mapTileRequestFailedExceedsMaxQueueSize(final MapTileRequestState aState) {
        }

        @Override
        public void mapTileRequestExpiredTile(final MapTileRequestState aState, final Drawable aDrawable) {
        }

        @Override
        public boolean useDataConnection() {
            return false;
        }
    };

    private final MapTileModuleProviderBase mTileProvider = new MapTileModuleProviderBase(1, 10) {

        @Override
        protected String getThreadGroupName() {
            return "OpenStreetMapAsyncTileProviderTest";
        }

        @Override
        public TileLoader getTileLoader() {
            return new TileLoader() {
                @Override
                public Drawable loadTile(final long pMapTileIndex)
                        throws CantContinueException {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                    }
                    return new BitmapDrawable();
                }
            };
        }

        @Override
        public boolean getUsesDataConnection() {
            return false;
        }

        @Override
        public int getMinimumZoomLevel() {
            return 0;
        }

        @Override
        public int getMaximumZoomLevel() {
            return 10;
        }

        @Override
        protected String getName() {
            return "test";
        }

        @Override
        public void setTileSource(final ITileSource pTileSource) {
            // Do nothing
        }
    };

    @Test
    public void test_put_twice() {

        final long tile = MapTileIndex.getTileIndex(1, 1, 1);

        // request the same tile twice
        final MapTileRequestState state = new MapTileRequestState(tile, mProviders,
                mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state);
        mTileProvider.loadMapTileAsync(state);

        // check that is only one tile pending
        assertEquals("One tile pending", 1, mTileProvider.mPending.size());
    }

    /**
     * Test that the tiles are loaded in most recently accessed order.
     */
    @Test
    public void test_order() throws InterruptedException {

        final long tile1 = MapTileIndex.getTileIndex(1, 1, 1);
        final long tile2 = MapTileIndex.getTileIndex(2, 2, 2);
        final long tile3 = MapTileIndex.getTileIndex(3, 3, 3);

        // request the three tiles
        final MapTileRequestState state1 = new MapTileRequestState(tile1,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state1);
        Thread.sleep(100); // give the thread time to run
        final MapTileRequestState state2 = new MapTileRequestState(tile2,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state2);
        Thread.sleep(100); // give the thread time to run
        final MapTileRequestState state3 = new MapTileRequestState(tile3,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state3);

        // wait up to 10 seconds (because it takes 1 second for each tile + an extra
        // second)

        long timeout = System.currentTimeMillis() + 10000;
        while (3 != mTiles.size() && System.currentTimeMillis() < timeout) {
            Thread.sleep(250);
        }

        // check that there are three tiles in the list (ie no duplicates)
        assertEquals("Three tiles in the list", 3, mTiles.size());

        // the tiles should have been loaded in the order 1, 3, 2
        // because 1 was loaded immediately, 2 was next,
        // but 3 was requested before 2 started, so it jumped the queue
        assertEquals("tile1 is first", tile1, (long) mTiles.get(0));
        assertEquals("tile3 is second", tile3, (long) mTiles.get(1));
        assertEquals("tile2 is third", tile2, (long) mTiles.get(2));
    }

    /**
     * Test that adding the same tile more than once moves it up the queue.
     */
    @Test
    public void test_jump_queue() throws InterruptedException {
        final long tile1 = MapTileIndex.getTileIndex(1, 1, 1);
        final long tile2 = MapTileIndex.getTileIndex(2, 2, 2);
        final long tile3 = MapTileIndex.getTileIndex(3, 3, 3);

        // request tile1, tile2, tile3, then tile2 again
        final MapTileRequestState state1 = new MapTileRequestState(tile1,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state1);
        Thread.sleep(100); // give the thread time to run
        final MapTileRequestState state2 = new MapTileRequestState(tile2,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state2);
        Thread.sleep(100); // give the thread time to run
        final MapTileRequestState state3 = new MapTileRequestState(tile3,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state3);
        Thread.sleep(100); // give the thread time to run
        final MapTileRequestState state4 = new MapTileRequestState(tile2,
                mProviders, mTileProviderCallback);
        mTileProvider.loadMapTileAsync(state4);

        // wait up to 10 seconds (because it takes 1 second for each tile + an extra
        // second)
        long timeout = System.currentTimeMillis() + 10000;
        while (3 != mTiles.size() && System.currentTimeMillis() < timeout) {
            Thread.sleep(250);
        }

        // check that there are three tiles in the list (ie no duplicates)
        assertEquals("Three tiles in the list", 3, mTiles.size());

        // the tiles should have been loaded in the order 1, 2, 3
        // 3 jumped ahead of 2, but then 2 jumped ahead of it again
        assertEquals("tile1 is first", tile1, (long) mTiles.get(0));
        assertEquals("tile2 is second", tile2, (long) mTiles.get(1));
        assertEquals("tile3 is third", tile3, (long) mTiles.get(2));
    }
}

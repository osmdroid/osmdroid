package org.osmdroid.tileprovider;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.util.MapTileArea;
import org.osmdroid.util.MapTileIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests related to {@link MapTileCache}
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class MapTileCacheTest {

    private final int mZoom = 10;

    @Test
    public void testCapacity() {
        final Drawable drawable = getNonNullDrawable();
        final int capacity = 50;
        final int extra = 4;
        final int extraExtra = 3;
        final MapTileCache mapTileCache = new MapTileCache(capacity);
        final MapTileArea mapTileArea = mapTileCache.getMapTileArea();

        // init: the cache is empty
        Assert.assertEquals(0, mapTileCache.getSize());

        // inserting items: the sizes should match
        for (int i = 0; i < capacity + extra; i++) {
            mapTileCache.putTile(getMapTileIndex(i), drawable);
        }
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // same keys: the size is not supposed to grow
        for (int i = 0; i < capacity + extra; i++) {
            mapTileCache.putTile(getMapTileIndex(i), drawable);
        }
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with very big number of protected tiles: no tiles are removed
        mapTileArea.set(mZoom, 0, 0, 0, capacity + extra + extraExtra - 1);
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with all protected tiles: no tiles are removed
        mapTileArea.set(mZoom, 0, 0, 0, capacity + extra - 1);
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with not all protected tiles: tiles are removed up to capacity
        mapTileArea.set(mZoom, 0, 0, 0, capacity - 1);
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity, mapTileCache.getSize());
        for (int i = 0; i < capacity + extra; i++) {
            final Drawable value = mapTileCache.getMapTile(getMapTileIndex(i));
            if (i < capacity) {
                Assert.assertNotNull(value);
            } else {
                Assert.assertNull(value);
            }
        }

        // garbage collection without protected tiles: tiles are removed up to capacity
        mapTileArea.reset();
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity, mapTileCache.getSize());

        // clear: the cache is now empty
        mapTileCache.clear();
        Assert.assertEquals(0, mapTileCache.getSize());
    }

    private long getMapTileIndex(final int pIndex) {
        return MapTileIndex.getTileIndex(mZoom, 0, pIndex);
    }

    private Drawable getNonNullDrawable() {
        return new Drawable() {
            @Override
            public void draw(Canvas canvas) {

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        final MapTileCache mapTileCache = new MapTileCache(100);
        List<Thread> threads = new ArrayList<>();
        final Drawable dummyDrawable = new ColorDrawable(0x0);

        final int NUM_TILES = 10000;
        for (int i = 0; i < NUM_TILES; i++) {
            mapTileCache.putTile(i, dummyDrawable);
        }

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < NUM_TILES; j++) {
                        mapTileCache.remove(j);
                    }
                }
            };
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads)
            thread.join();

        mapTileCache.clear();
        Assert.assertEquals(0, mapTileCache.getSize());
    }
}

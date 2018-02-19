package org.osmdroid.tileprovider;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import junit.framework.Assert;

import org.junit.Test;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.MapTileList;

/**
 * Unit tests related to {@link MapTileCache}
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class MapTileCacheTest {

    @Test
    public void testCapacity() {
        final Drawable drawable = getNonNullDrawable();
        final int capacity = 50;
        final int extra = 4;
        final int extraExtra = 3;
        final MapTileCache mapTileCache = new MapTileCache(capacity);
        final MapTileList mapTileList = mapTileCache.getMapTileList();

        // init: the cache is empty
        Assert.assertEquals(0, mapTileCache.getSize());

        // inserting items: the sizes should match
        for (int i = 0 ; i < capacity + extra ; i ++) {
            mapTileCache.putTile(getMapTileIndex(i), drawable);
        }
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // same keys: the size is not supposed to grow
        for (int i = 0 ; i < capacity + extra ; i ++) {
            mapTileCache.putTile(getMapTileIndex(i), drawable);
        }
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with very big number of protected tiles: no tiles are removed
        mapTileList.clear();
        for (int i = 0 ; i < capacity + extra + extraExtra ; i ++) {
            mapTileList.put(getMapTileIndex(i));
        }
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with all protected tiles: no tiles are removed
        mapTileList.clear();
        for (int i = 0 ; i < capacity + extra ; i ++) {
            mapTileList.put(getMapTileIndex(i));
        }
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity + extra, mapTileCache.getSize());

        // garbage collection with not all protected tiles: tiles are removed up to capacity
        mapTileList.clear();
        for (int i = 0 ; i < capacity ; i ++) {
            mapTileList.put(getMapTileIndex(i));
        }
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity, mapTileCache.getSize());
        for (int i = 0 ; i < capacity + extra ; i ++) {
            final Drawable value = mapTileCache.getMapTile(getMapTileIndex(i));
            if (i < capacity) {
                Assert.assertNotNull(value);
            } else {
                Assert.assertNull(value);
            }
        }

        // garbage collection without protected tiles: tiles are removed up to capacity
        mapTileList.clear();
        mapTileCache.garbageCollection();
        Assert.assertEquals(capacity, mapTileCache.getSize());

        // clear: the cache is now empty
        mapTileCache.clear();
        Assert.assertEquals(0, mapTileCache.getSize());
    }

    private long getMapTileIndex(final int pIndex) {
        final int zoom = 10;
        return MapTileIndex.getTileIndex(zoom, pIndex, pIndex);
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
}

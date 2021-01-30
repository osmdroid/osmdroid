package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Random;

/**
 * Unit tests related to {@link MapTileIndex}
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class MapTileIndexTest {

    private static final Random random = new Random();

    @Test
    public void testIndex() {
        final int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            final int zoom = getRandomZoom();
            final int x = getRandomXY(zoom);
            final int y = getRandomXY(zoom);
            final long index = MapTileIndex.getTileIndex(zoom, x, y);
            checkIndex(index, zoom, x, y);
        }
    }

    private void checkIndex(final long pIndex, final int pZoom, final int pX, final int pY) {
        Assert.assertEquals(pZoom, MapTileIndex.getZoom(pIndex));
        Assert.assertEquals(pX, MapTileIndex.getX(pIndex));
        Assert.assertEquals(pY, MapTileIndex.getY(pIndex));
    }

    private int getRandomZoom() {
        return random.nextInt(TileSystem.primaryKeyMaxZoomLevel + 1);
    }

    private int getRandomXY(final int pZoom) {
        return random.nextInt(1 << pZoom);
    }
}

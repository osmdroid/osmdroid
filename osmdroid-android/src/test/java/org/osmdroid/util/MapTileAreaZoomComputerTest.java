package org.osmdroid.util;

import android.graphics.Rect;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit tests related to {@link MapTileAreaZoomComputer}
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaZoomComputerTest {

    @Test
    public void testWorld() {
        final MapTileArea src = new MapTileArea();
        final MapTileArea dst = new MapTileArea();
        long size;
        int mapTileUpperBound;
        for (int zoom = 0; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            mapTileUpperBound = getMapTileUpperBound(zoom);
            size = ((long) mapTileUpperBound) * mapTileUpperBound;
            if (size >= Integer.MAX_VALUE) {
                return;
            }
            for (int i = 0; i <= 1; i++) {
                final Rect rect = new Rect();
                switch (i) {
                    case 0: // the world
                        rect.left = 0; // remember: don't use the rect.set() syntax for unit test (cf. ProjectionTest)
                        rect.top = 0;
                        rect.right = mapTileUpperBound - 1;
                        rect.bottom = mapTileUpperBound - 1;
                        break;
                    case 1: // top-left quarter of the world
                        if (zoom == 0) { // top-left quarter makes no sense in zoom 0
                            continue;
                        }
                        rect.left = 0;
                        rect.top = 0;
                        rect.right = mapTileUpperBound / 2 - 1;
                        rect.bottom = mapTileUpperBound / 2 - 1;
                        break;
                }
                src.set(zoom, rect);
                final long srcSize = src.size();
                for (int zoomDelta = 0; zoomDelta <= TileSystem.getMaximumZoomLevel(); zoomDelta++) {
                    final int newZoom = zoom + zoomDelta;
                    if (newZoom < 0 || newZoom > TileSystem.getMaximumZoomLevel()) {
                        continue;
                    }
                    mapTileUpperBound = getMapTileUpperBound(newZoom);
                    size = ((long) mapTileUpperBound) * mapTileUpperBound;
                    if (size >= Integer.MAX_VALUE) {
                        return;
                    }
                    final MapTileAreaZoomComputer computer = new MapTileAreaZoomComputer(zoomDelta);
                    computer.computeFromSource(src, dst);
                    final long dstSize = dst.size();
                    final String message = "zoom=" + zoom + ", delta=" + zoomDelta;
                    if (zoomDelta == 0) {
                        Assert.assertEquals(message, srcSize, dstSize);
                    } else if (zoomDelta < 0) {
                        Assert.assertEquals(message, srcSize * (1 >> -zoomDelta) * (1 >> -zoomDelta), dstSize);
                    } else {
                        Assert.assertEquals(message, srcSize * (1 << zoomDelta) * (1 << zoomDelta), dstSize);
                    }
                }
            }
        }
    }

    /**
     * @since 6.1.0
     */
    @Test
    public void testBugANRSideEffect() {
        final MapTileArea source = new MapTileArea();
        final MapTileArea dest = new MapTileArea();
        source.set(0, 0, 0, 1, 1);
        final MapTileAreaZoomComputer computer = new MapTileAreaZoomComputer(-1);
        computer.computeFromSource(source, dest);
        Assert.assertEquals(0, dest.getWidth());
    }

    private int getMapTileUpperBound(final int pZoom) {
        return 1 << pZoom;
    }
}

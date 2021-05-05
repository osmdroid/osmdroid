package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Unit tests related to {@link MapTileAreaList}
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaListTest {

    private Random mRandom = new Random();

    @Test
    public void test() {
        final int decentMax = 10;
        final MapTileAreaList list = new MapTileAreaList();
        Assert.assertEquals(0, list.size());

        final List<Integer> counts = new ArrayList<>();
        int count = 0;
        for (int zoom = 0; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            final int mapTileUpperBound = getMapTileUpperBound(zoom);
            final int number = mRandom.nextInt(mapTileUpperBound) % decentMax;
            final int size = (number + 1) * (number + 1);
            counts.add(size);
            count += size;
            list.getList().add(new MapTileArea().set(zoom, 0, 0, number, number));
            Assert.assertEquals(count, list.size());
            for (int x = 0; x <= number; x++) {
                for (int y = 0; y <= number; y++) {
                    Assert.assertTrue(list.contains(MapTileIndex.getTileIndex(zoom, x, y)));
                }
            }
        }

        // checking the number of tiles per zoom and the zoom order
        int zoom = -1;
        count = 0;
        int total = 0;
        for (final long mapTileIndex : list) {
            total++;
            final int newZoom = MapTileIndex.getZoom(mapTileIndex);
            if (zoom != newZoom) {
                if (zoom != -1) {
                    Assert.assertEquals((int) counts.get(zoom), count);
                }
                count = 0;
                zoom = newZoom;
            }
            count++;
        }
        Assert.assertEquals((int) counts.get(zoom), count);
        Assert.assertEquals(list.size(), total);
    }

    private int getMapTileUpperBound(final int pZoom) {
        return 1 << pZoom;
    }
}

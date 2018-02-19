package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Unit tests related to {@link MapTileList}
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class MapTileListTest {

    private static final Random random = new Random();

    @Test
    public void testGetPut() {
        final int iterations = 100;
        final int maxSize = 20;
        for (int i = 0 ; i < iterations ; i ++) {
            final int size = 1 + random.nextInt(maxSize);
            final long[] array = new long[size];
            for (int j = 0 ; j < size ; j ++) {
                array[j] = random.nextLong();
            }
            final MapTileList list = new MapTileList();
            for (int j = 0 ; j < size ; j ++) {
                list.put(array[j]);
            }
            check(array, list);
            list.clear();
            check(new long[0], list);
        }
    }

    private void check(final HashSet<Long> pSet, final int pZoom,
                       final int pXMin, final int pXMax, final int pYMin, final int pYMax) {
        Assert.assertEquals((pXMax - pXMin + 1) * (pYMax - pYMin + 1), pSet.size());
        for (int expectedX = pXMin ; expectedX <= pXMax ; expectedX ++) {
            for (int expectedY = pYMax ; expectedY <= pYMax ; expectedY ++) {
                Assert.assertTrue(pSet.contains(MapTileIndex.getTileIndex(pZoom, expectedX, expectedY)));
            }
        }
    }

    private void check(final long[] pArray, final MapTileList pList) {
        Assert.assertEquals(pArray.length, pList.getSize());
        for (int i = 0 ; i < pArray.length ; i ++) {
            Assert.assertEquals(pArray[i], pList.get(i));
        }
    }

    @Test
    public void testPopulateFrom() {
        final MapTileList source = new MapTileList();
        final MapTileList dest = new MapTileList();
        final HashSet<Long> set = new HashSet<>();
        final int sourceZoom = 5;
        final int sourceXMin = 10;
        final int sourceXMax = 15;
        final int sourceYMin = 20;
        final int sourceYMax = 22;
        final int destMinus1XMin = sourceXMin >> 1;
        final int destMinus1XMax = sourceXMax >> 1;
        final int destMinus1YMin = sourceYMin >> 1;
        final int destMinus1YMax = sourceYMax >> 1;
        final int destPlus1XMin = sourceXMin << 1;
        final int destPlus1XMax = (sourceXMax << 1) + 1;
        final int destPlus1YMin = sourceYMin << 1;
        final int destPlus1YMax = (sourceYMax << 1) + 1;
        for (int i = sourceXMin ; i <= sourceXMax ; i ++) {
            for (int j = sourceYMin ; j <= sourceYMax ; j ++) {
                source.put(MapTileIndex.getTileIndex(sourceZoom, i, j));
            }
        }
        Assert.assertEquals((sourceXMax - sourceXMin + 1) * (sourceYMax - sourceYMin + 1), source.getSize());

        // count checking
        final int minMaxDelta = 4;
        for (int zoomDelta = -minMaxDelta ; zoomDelta < minMaxDelta ; zoomDelta ++) {
            dest.clear();
            dest.populateFrom(source, zoomDelta);
            final String tag = "zoomDelta=" + zoomDelta;
            if (sourceZoom + zoomDelta < 0 || sourceZoom + zoomDelta > MapTileIndex.mMaxZoomLevel) {
                Assert.assertEquals(tag, 0, dest.getSize());
            } else if (zoomDelta <= 0) {
                Assert.assertEquals(tag, source.getSize(), dest.getSize());
            } else {
                Assert.assertEquals(tag, source.getSize() << (2 * zoomDelta), dest.getSize());
            }
        }

        int zoomDelta;
        // data checking for -1
        zoomDelta = -1;
        dest.clear();
        dest.populateFrom(source, zoomDelta);
        set.clear();
        populateSet(set, dest);
        check(set, sourceZoom + zoomDelta, destMinus1XMin, destMinus1XMax, destMinus1YMin, destMinus1YMax);

        // data checking for +1
        zoomDelta = 1;
        dest.clear();
        dest.populateFrom(source, zoomDelta);
        set.clear();
        populateSet(set, dest);
        check(set, sourceZoom + zoomDelta, destPlus1XMin, destPlus1XMax, destPlus1YMin, destPlus1YMax);
    }

    private void populateSet(final Set<Long> pSet, final MapTileList pMapTileList) {
        for (int i = 0 ; i < pMapTileList.getSize() ; i ++) {
            pSet.add(pMapTileList.get(i));
        }
    }
}

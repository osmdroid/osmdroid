package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests related to {@link MapTileListZoomComputer}
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 * @deprecated Use {@link MapTileAreaZoomComputerTest} instead
 */

@Deprecated
public class MapTileListZoomComputerTest {

    @Test
    public void testComputeFromSource() {
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
        for (int i = sourceXMin; i <= sourceXMax; i++) {
            for (int j = sourceYMin; j <= sourceYMax; j++) {
                source.put(MapTileIndex.getTileIndex(sourceZoom, i, j));
            }
        }
        Assert.assertEquals((sourceXMax - sourceXMin + 1) * (sourceYMax - sourceYMin + 1), source.getSize());

        // count checking
        final int minMaxDelta = 4;
        for (int zoomDelta = -minMaxDelta; zoomDelta < minMaxDelta; zoomDelta++) {
            final MapTileListZoomComputer computer = new MapTileListZoomComputer(zoomDelta);
            dest.clear();
            computer.computeFromSource(source, dest);
            final String tag = "zoomDelta=" + zoomDelta;
            if (sourceZoom + zoomDelta < 0 || sourceZoom + zoomDelta > MapTileIndex.mMaxZoomLevel) {
                Assert.assertEquals(tag, 0, dest.getSize());
            } else if (zoomDelta <= 0) {
                Assert.assertEquals(tag, source.getSize(), dest.getSize());
            } else {
                Assert.assertEquals(tag, source.getSize() << (2 * zoomDelta), dest.getSize());
            }
        }

        MapTileListZoomComputer computer;
        // data checking for -1
        computer = new MapTileListZoomComputer(-1);
        dest.clear();
        computer.computeFromSource(source, dest);
        set.clear();
        populateSet(set, dest);
        check(set, sourceZoom + computer.getZoomDelta(), destMinus1XMin, destMinus1XMax, destMinus1YMin, destMinus1YMax);

        // data checking for +1
        computer = new MapTileListZoomComputer(1);
        dest.clear();
        computer.computeFromSource(source, dest);
        set.clear();
        populateSet(set, dest);
        check(set, sourceZoom + computer.getZoomDelta(), destPlus1XMin, destPlus1XMax, destPlus1YMin, destPlus1YMax);
    }

    private void check(final HashSet<Long> pSet, final int pZoom,
                       final int pXMin, final int pXMax, final int pYMin, final int pYMax) {
        Assert.assertEquals((pXMax - pXMin + 1) * (pYMax - pYMin + 1), pSet.size());
        for (int expectedX = pXMin; expectedX <= pXMax; expectedX++) {
            for (int expectedY = pYMax; expectedY <= pYMax; expectedY++) {
                Assert.assertTrue(pSet.contains(MapTileIndex.getTileIndex(pZoom, expectedX, expectedY)));
            }
        }
    }

    private void populateSet(final Set<Long> pSet, final MapTileList pMapTileList) {
        for (int i = 0; i < pMapTileList.getSize(); i++) {
            pSet.add(pMapTileList.get(i));
        }
    }
}

package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests related to {@link MapTileAreaBorderComputer}
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaBorderComputerTest {

    /**
     * Checking border on one point, with modulo side effects
     */
    @Test
    public void testOnePointModulo() {
        final MapTileArea source = new MapTileArea();
        final MapTileArea dest = new MapTileArea();
        final Set<Long> set = new HashSet<>();
        final int border = 2;
        final MapTileAreaBorderComputer computer = new MapTileAreaBorderComputer(border);
        final int zoom = 5;
        final int sourceX = 1;
        final int sourceY = 31;
        source.set(zoom, sourceX, sourceY, sourceX, sourceY);
        add(set, zoom, sourceX, sourceY, border);
        computer.computeFromSource(source, dest);
        check(dest, set, zoom);
    }

    /**
     * Checking border on two contiguous points, with modulo side effects
     */
    @Test
    public void testTwoContiguousPointsModulo() {
        final MapTileArea source = new MapTileArea();
        final MapTileArea dest = new MapTileArea();
        final Set<Long> set = new HashSet<>();
        final int border = 2;
        final MapTileAreaBorderComputer computer = new MapTileAreaBorderComputer(border);
        final int zoom = 5;
        final int sourceX = 1;
        final int sourceY = 31;
        source.set(zoom, sourceX, sourceY, sourceX + 1, sourceY);
        add(set, zoom, sourceX, sourceY, border);
        add(set, zoom, sourceX + 1, sourceY, border);
        computer.computeFromSource(source, dest);
        check(dest, set, zoom);
    }

    private void check(final MapTileArea pArea, final Set<Long> pSet, final int pZoom) {
        checkUnique(pArea);
        checkZoom(pArea, pZoom);
        checkEquals(pArea, pSet);
    }

    private void checkEquals(final MapTileArea pArea, final Set<Long> pSet) {
        Assert.assertEquals(pSet.size(), pArea.size());
        for (final long mapTileIndex : pArea) {
            Assert.assertTrue(pSet.contains(mapTileIndex));
        }
    }

    private void checkUnique(final MapTileArea pArea) {
        final Set<Long> set = new HashSet<>();
        for (final long mapTileIndex : pArea) {
            Assert.assertTrue(set.add(mapTileIndex));
        }
    }

    private void checkZoom(final MapTileArea pArea, final int pZoom) {
        for (final long index : pArea) {
            Assert.assertEquals(pZoom, MapTileIndex.getZoom(index));
        }
    }

    private void add(final Set<Long> pSet, final int pZoom, final int pX, final int pY, final int pBorder) {
        final int power = 1 << pZoom;
        for (int i = pX - pBorder; i <= pX + pBorder; i++) {
            for (int j = pY - pBorder; j <= pY + pBorder; j++) {
                pSet.add(MapTileIndex.getTileIndex(pZoom, (i + power) % power, (j + power) % power));
            }
        }
    }
}

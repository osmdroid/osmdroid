package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests related to {@link MapTileListBorderComputer}
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 * @deprecated Use {@link MapTileAreaBorderComputerTest} instead
 */

@Deprecated
public class MapTileListBorderComputerTest {

    /**
     * Checking border on one point, with modulo side effects, in "include all" mode on
     */
    @Test
    public void testOnePointModuloInclude() {
        final MapTileList source = new MapTileList();
        final MapTileList dest = new MapTileList();
        final Set<Long> set = new HashSet<>();
        final int border = 2;
        final MapTileListBorderComputer computer = new MapTileListBorderComputer(border, true);
        final int zoom = 5;
        final int sourceX = 1;
        final int sourceY = 31;
        source.put(MapTileIndex.getTileIndex(zoom, sourceX, sourceY));
        add(set, zoom, sourceX, sourceY, border);
        computer.computeFromSource(source, dest);
        check(dest, set, zoom);
    }

    /**
     * Checking border on one point, with modulo side effects, in "include all" mode off
     */
    @Test
    public void testOnePointModulo() {
        final MapTileList source = new MapTileList();
        final MapTileList dest = new MapTileList();
        final Set<Long> set = new HashSet<>();
        final int border = 2;
        final MapTileListBorderComputer computer = new MapTileListBorderComputer(border, false);
        final int zoom = 5;
        final int sourceX = 1;
        final int sourceY = 31;
        source.put(MapTileIndex.getTileIndex(zoom, sourceX, sourceY));
        add(set, zoom, sourceX, sourceY, border);
        set.remove(MapTileIndex.getTileIndex(zoom, sourceX, sourceY));
        computer.computeFromSource(source, dest);
        check(dest, set, zoom);
    }

    /**
     * Checking border on two contiguous points, with modulo side effects, in "include all" mode on
     */
    @Test
    public void testTwoContiguousPointsModuloInclude() {
        final MapTileList source = new MapTileList();
        final MapTileList dest = new MapTileList();
        final Set<Long> set = new HashSet<>();
        final int border = 2;
        final MapTileListBorderComputer computer = new MapTileListBorderComputer(border, true);
        final int zoom = 5;
        final int sourceX = 1;
        final int sourceY = 31;
        source.put(MapTileIndex.getTileIndex(zoom, sourceX, sourceY));
        source.put(MapTileIndex.getTileIndex(zoom, sourceX + 1, sourceY));
        add(set, zoom, sourceX, sourceY, border);
        add(set, zoom, sourceX + 1, sourceY, border);
        computer.computeFromSource(source, dest);
        check(dest, set, zoom);
    }

    private void check(final MapTileList pMapTileList, final Set<Long> pSet, final int pZoom) {
        checkUnique(pMapTileList);
        checkZoom(pMapTileList, pZoom);
        checkEquals(pMapTileList, pSet);
    }

    private void checkEquals(final MapTileList pMapTileList, final Set<Long> pSet) {
        Assert.assertEquals(pSet.size(), pMapTileList.getSize());
        for (int i = 0; i < pMapTileList.getSize(); i++) {
            Assert.assertTrue(pSet.contains(pMapTileList.get(i)));
        }
    }

    private void checkUnique(final MapTileList pMapTileList) {
        final Set<Long> set = new HashSet<>();
        for (int i = 0; i < pMapTileList.getSize(); i++) {
            Assert.assertTrue(set.add(pMapTileList.get(i)));
        }
    }

    private void checkZoom(final MapTileList pMapTileList, final int pZoom) {
        for (int i = 0; i < pMapTileList.getSize(); i++) {
            final long index = pMapTileList.get(i);
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

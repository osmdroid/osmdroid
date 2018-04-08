package org.osmdroid.tileprovider.cachemanager;

import android.graphics.Point;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.MyMath;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.osmdroid.tileprovider.cachemanager.CacheManager.getMapTileFromCoordinates;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CacheManagerTest {
    /**
     * Make sure {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverageIterable(BoundingBox, int, int)} returns the
     * same size and elements as the {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverage(BoundingBox, int, int)}
     * for a big mapTileUpperBound
     */
    @Test
    public void testGetTilesIterableForBigMapTileUpperBound() {
        verifyGetTilesIterable(15, 15);
    }

    /**
     * Make sure {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverageIterable(BoundingBox, int, int)} returns the
     * same size and elements as the {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverage(BoundingBox, int, int)}
     * for a small mapTileUpperBound
     */
    @Test
    public void testGetTilesIterableForSmallMapTileUpperBound() {
        verifyGetTilesIterable(2, 2);
    }

    /**
     * Make sure {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverageIterable(BoundingBox, int, int)} returns the
     * same size and elements as the {@link org.osmdroid.tileprovider.cachemanager.CacheManager#getTilesCoverage(BoundingBox, int, int)}
     * for the range of zoom levels.
     */
    @Test
    public void testGetTilesIterableForRangeOfZooms() {
        verifyGetTilesIterable(10, 11);
    }

    private void verifyGetTilesIterable(int pMinZoom, int pMaxZoom) {
        BoundingBox boundingBox = new BoundingBox(52.95131467958858, 13.6473953271975,
                52.886830733534954, 13.3473953271975);
        //extract the collection using the current way
        List<Long> allPointsList = new ArrayList<>();
        for (int zoomLevel = pMinZoom; zoomLevel <= pMaxZoom; zoomLevel++) {
            allPointsList.addAll(getTilesCoverage(boundingBox, zoomLevel));
        }
        Iterator<Long> allPointsIterator = allPointsList.iterator();

        //retrieve an iterator for returning points in the "lazy" way
        IterableWithSize iterableWithSize = CacheManager.getTilesCoverageIterable(boundingBox,
                pMinZoom, pMaxZoom);

        //confirm both iterables contain the same number of elements
        Assert.assertEquals(allPointsList.size(), iterableWithSize.size());
        //confirm both iterables return the same elements
        for (Long value : org.osmdroid.tileprovider.cachemanager.CacheManager.getTilesCoverageIterable(boundingBox, pMinZoom, pMaxZoom)) {
            Assert.assertEquals(allPointsIterator.next(), value);
        }
    }

    /**
     * An old way of collecting tile points for the given zoom. It's kept purely for testing against
     * the next implementation.
     * @param pBB the given bounding box
     * @param pZoomLevel the given zoom level
     * @return the collection of matching points
     */
    private static Collection<Long> getTilesCoverage(final BoundingBox pBB, final int pZoomLevel){
        final Set<Long> result = new LinkedHashSet<>();
        final int mapTileUpperBound = 1 << pZoomLevel;
        final Point lowerRight = getMapTileFromCoordinates(
                pBB.getLatSouth(), pBB.getLonEast(), pZoomLevel);
        final Point upperLeft = getMapTileFromCoordinates(
                pBB.getLatNorth(), pBB.getLonWest(), pZoomLevel);
        int width = lowerRight.x - upperLeft.x + 1; // handling the modulo
        if (width <= 0) {
            width += mapTileUpperBound;
        }
        int height = lowerRight.y - upperLeft.y + 1; // handling the modulo
        if (height <= 0) {
            height += mapTileUpperBound;
        }
        for (int i = 0 ; i < width ; i ++) {
            for (int j = 0 ; j < height ; j ++) {
                final int x = MyMath.mod(upperLeft.x + i, mapTileUpperBound);
                final int y = MyMath.mod(upperLeft.y + j, mapTileUpperBound);
                result.add(MapTileIndex.getTileIndex(pZoomLevel, x, y));
            }
        }
        return result;
    }
}

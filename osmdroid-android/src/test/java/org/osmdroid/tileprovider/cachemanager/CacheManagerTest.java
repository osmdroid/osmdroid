package org.osmdroid.tileprovider.cachemanager;

import android.graphics.Rect;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.IterableWithSize;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CacheManagerTest {

    private final Random mRandom = new Random();

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
        IterableWithSize<Long> iterableWithSize = CacheManager.getTilesCoverageIterable(boundingBox,
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
     *
     * @param pBB        the given bounding box
     * @param pZoomLevel the given zoom level
     * @return the collection of matching points
     */
    private static Collection<Long> getTilesCoverage(final BoundingBox pBB, final int pZoomLevel) {
        final Set<Long> result = new LinkedHashSet<>();
        final int mapTileUpperBound = 1 << pZoomLevel;
        final Rect rect = CacheManager.getTilesRect(pBB, pZoomLevel);
        for (int j = rect.top; j <= rect.bottom; j++) {
            for (int i = rect.left; i <= rect.right; i++) { // x incrementing first for the test
                final int x = MyMath.mod(i, mapTileUpperBound);
                final int y = MyMath.mod(j, mapTileUpperBound);
                result.add(MapTileIndex.getTileIndex(pZoomLevel, x, y));
            }
        }
        return result;
    }

    /**
     * @since 6.0.3
     */
    @Test
    public void testGetTilesRectSingleTile() {
        final TileSystem tileSystem = MapView.getTileSystem();
        final BoundingBox box = new BoundingBox();
        for (int zoom = 0; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            final double longitude = tileSystem.getRandomLongitude(mRandom.nextDouble());
            final double latitude = tileSystem.getRandomLatitude(mRandom.nextDouble());
            box.set(latitude, longitude, latitude, longitude); // single point
            final Rect rect = CacheManager.getTilesRect(box, zoom);
            Assert.assertEquals(rect.left, rect.right); // single tile expected
            Assert.assertEquals(rect.top, rect.bottom); // single tile expected
        }
    }

    /**
     * @since 6.0.3
     */
    @Test
    public void testGetTilesRectWholeWorld() {
        final TileSystem tileSystem = MapView.getTileSystem();
        final BoundingBox box = new BoundingBox( // whole world
                tileSystem.getMaxLatitude(), tileSystem.getMaxLongitude(),
                tileSystem.getMinLatitude(), tileSystem.getMinLongitude());
        for (int zoom = 0; zoom <= TileSystem.getMaximumZoomLevel(); zoom++) {
            final Rect rect = CacheManager.getTilesRect(box, zoom);
            Assert.assertEquals(0, rect.left);
            Assert.assertEquals(0, rect.top);
            final int maxSize = -1 + (1 << zoom);
            Assert.assertEquals(maxSize, rect.bottom);
            Assert.assertEquals(maxSize, rect.right);
        }
    }
}

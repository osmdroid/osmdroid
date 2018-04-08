package org.osmdroid.tileprovider.cachemanager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.BoundingBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;
import java.util.Iterator;

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

    private void verifyGetTilesIterable(int minZoom, int maxZoom) {
        BoundingBox boundingBox = new BoundingBox(52.95131467958858, 13.6473953271975,
                52.886830733534954, 13.3473953271975);
        //extract the collection using the current way
        Collection<Long> allPointsCollection = CacheManager.getTilesCoverage(boundingBox, minZoom,
                maxZoom);
        Iterator<Long> allPointsIterator = allPointsCollection.iterator();

        //retrieve an iterator for returning points in the "lazy" way
        CacheManager.IterableWithSize iterableWithSize = CacheManager.getTilesCoverageIterable(boundingBox,
                minZoom, maxZoom);

        //confirm both iterables contain the same number of elements
        Assert.assertEquals(allPointsCollection.size(), iterableWithSize.size());
        //confirm both iterables return the same elements
        for (Long value : org.osmdroid.tileprovider.cachemanager.CacheManager.getTilesCoverageIterable(boundingBox, minZoom, maxZoom)) {
            Assert.assertEquals(allPointsIterator.next(), value);
        }
    }
}

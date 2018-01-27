package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Random;

import microsoft.mappoint.TileSystem;

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
            checkList(array, list);
            list.clear();
            checkList(new long[0], list);
        }
    }

    private void checkList(final long[] pArray, final MapTileList pList) {
        Assert.assertEquals(pArray.length, pList.getSize());
        for (int i = 0 ; i < pArray.length ; i ++) {
            Assert.assertEquals(pArray[i], pList.get(i));
        }
    }
}

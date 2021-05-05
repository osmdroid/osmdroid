package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Random;

/**
 * Created by Fabrice on 31/12/2017.
 *
 * @since 6.0.0
 */

public class ListPointLTest {

    private static final Random random = new Random();

    @Test
    public void test() throws Exception {
        final int size = 100;
        final long[] values = new long[2 * size];
        final ListPointL list = new ListPointL();
        Assert.assertEquals(0, list.size());
        reload(values);
        check(values, list);
        list.clear();
        reload(values);
        check(values, list);
        list.clear();
        Assert.assertEquals(0, list.size());
    }

    private void reload(final long[] pValues) {
        for (int i = 0; i < pValues.length; i++) {
            pValues[i] = random.nextInt();
        }
    }

    private void check(final long[] pValues, final ListPointL pList) {
        int i;
        for (i = 0; i < pValues.length; i += 2) {
            pList.add(pValues[i], pValues[i + 1]);
        }
        i = 0;
        for (final PointL point : pList) {
            Assert.assertEquals(pValues[i], point.x);
            Assert.assertEquals(pValues[i + 1], point.y);
            i += 2;
        }
        Assert.assertEquals(pValues.length / 2, pList.size());
    }
}

package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test class for {@link SideOptimizationPointAccepter}
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */

public class SideOptimizationPointAccepterTest {

    class SimpleAccepter implements PointAccepter {

        private final List<PointL> mList = new ArrayList<>();

        public List<PointL> getList() {
            return mList;
        }

        @Override
        public void init() {
            mList.clear();
        }

        @Override
        public void add(long pX, long pY) {
            mList.add(new PointL(pX, pY));
        }

        @Override
        public void end() {
        }
    }

    @Test
    public void testNothing() { // never consecutive points with same X or same Y
        final long[] values = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        test(values, values);
    }

    @Test
    public void testOneSideX() {
        final long[] values = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, 5, // a column with X = 50
                2, 12};
        final long[] expected = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 4, 50, 20, 50, 5, // the optimized version
                2, 12};
        test(values, expected);
    }

    @Test
    public void testTwoSidesX() {
        final long[] values = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, 5, // a column with X = 50
                12, 2, 12, 78, 12, 3, 12, 1, 12, 1, 12, 4, // a column with X = 12
                2, 12};
        final long[] expected = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 4, 50, 20, 50, 5,
                12, 2, 12, 1, 12, 78, 12, 4,
                2, 12};
        test(values, expected);
    }

    @Test
    public void testOneSideY() {
        final long[] values = new long[]{
                0, 1, 2, 3,
                4, 50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, // a row with Y = 50
                5, 2};
        final long[] expected = new long[]{
                0, 1, 2, 3,
                4, 50, 20, 50, 18, 50,
                5, 2};
        test(values, expected);
    }

    @Test
    public void testTwoSidesY() {
        final long[] values = new long[]{
                0, 1, 2, 3,
                4, 50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, // a row with Y = 50
                45, 10, 16, 10, 2, 10, 14, 10, 1, 10, 8, 10, // a row with Y = 10
                5, 2};
        final long[] expected = new long[]{
                0, 1, 2, 3,
                4, 50, 20, 50, 18, 50,
                45, 10, 1, 10, 8, 10,
                5, 2};
        test(values, expected);
    }

    @Test
    public void testOneSideXManiac() {
        final long[] values = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 20, 50, 4, // columns with X = 50...
                50, 15, 50, 6, 50, 23, 50, 15, 50, 6, 50, 23, 50, 15, 50, 6, 50, 23,
                50, 15, 50, 6, 50, 23, 50, 15, 50, 6, 50, 23, 50, 15, 50, 6, 50, 23,
                50, 15, 50, 6, 50, 23, 50, 18, 50, 5,
                2, 12};
        final long[] expected = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 4, 50, 23, 50, 5,
                2, 12};
        test(values, expected);
    }

    @Test
    public void testRectangle() {
        final long[] values = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, 5, // a column with X = 50
                4, 50, 6, 50, 20, 50, 4, 50, 15, 50, 18, 50, // a row with Y = 50
                12, 2, 12, 78, 12, 3, 12, 1, 12, 1, 12, 4, // a column with X = 12
                5, 5,
                45, 10, 16, 10, 2, 10, 14, 10, 1, 10, 8, 10, // a row with Y = 10
                2, 12};
        final long[] expected = new long[]{
                1, 2, 3, 4,
                50, 6, 50, 4, 50, 20, 50, 5,
                4, 50, 20, 50, 18, 50,
                12, 2, 12, 1, 12, 78, 12, 4,
                5, 5,
                45, 10, 1, 10, 8, 10,
                2, 12};
        test(values, expected);
    }

    private void test(final long[] pValues, final long[] pExpected) {
        final SimpleAccepter simpleAccepter = new SimpleAccepter();
        final SideOptimizationPointAccepter optim = new SideOptimizationPointAccepter(simpleAccepter);
        optim.init();
        for (int i = 0; i < pValues.length; i += 2) {
            optim.add(pValues[i], pValues[i + 1]);
        }
        optim.end();
        final List<PointL> result = simpleAccepter.getList();
        final List<PointL> expected = new ArrayList<>();
        for (int i = 0; i < pExpected.length; i += 2) {
            expected.add(new PointL(pExpected[i], pExpected[i + 1]));
        }
        Assert.assertEquals(expected, result);
    }
}

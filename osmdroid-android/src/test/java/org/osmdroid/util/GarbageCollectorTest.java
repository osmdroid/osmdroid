package org.osmdroid.util;


import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class GarbageCollectorTest {

    /**
     * @since 6.1.3
     */
    private static final long ACTION_MILLISECONDS = 500;

    private final AtomicInteger mCount = new AtomicInteger(0);

    @Test
    public void testInit() {
        final GarbageCollector garbageCollector = new GarbageCollector(getAction());
        mCount.set(0);

        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(0, mCount.get());
    }

    @Test
    public void testFirst() {
        final GarbageCollector garbageCollector = new GarbageCollector(getAction());
        mCount.set(0);

        garbageCollector.gc();
        sleepFactor(.5);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleepFactor(2);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());
    }

    @Test
    public void testSecond() {
        final GarbageCollector garbageCollector = new GarbageCollector(getAction());
        mCount.set(0);

        garbageCollector.gc();
        sleepFactor(.5);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleepFactor(2);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());

        garbageCollector.gc();
        sleepFactor(.5);
        Assert.assertEquals(2, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleepFactor(2);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(2, mCount.get());
    }

    @Test
    public void testMulti() {
        final GarbageCollector garbageCollector = new GarbageCollector(getAction());
        mCount.set(0);

        garbageCollector.gc();
        garbageCollector.gc();
        garbageCollector.gc();
        garbageCollector.gc();
        sleepFactor(.5);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleepFactor(2);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());
    }

    private Runnable getAction() {
        return new Runnable() {
            @Override
            public void run() {
                mCount.incrementAndGet();
                sleepFactor(1);
            }
        };
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }

    /**
     * @since 6.1.3
     */
    private void sleepFactor(final double pFactor) {
        sleep(Math.round(ACTION_MILLISECONDS * pFactor));
    }
}

package org.osmdroid.util;

import junit.framework.Assert;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class GarbageCollectorTest {

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
        sleep(100);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleep(500);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());
    }

    @Test
    public void testSecond() {
        final GarbageCollector garbageCollector = new GarbageCollector(getAction());
        mCount.set(0);

        garbageCollector.gc();
        sleep(100);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleep(500);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());

        garbageCollector.gc();
        sleep(100);
        Assert.assertEquals(2, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleep(500);
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
        sleep(100);
        Assert.assertEquals(1, mCount.get());
        Assert.assertTrue(garbageCollector.isRunning());
        sleep(500);
        Assert.assertFalse(garbageCollector.isRunning());
        Assert.assertEquals(1, mCount.get());
    }

    private Runnable getAction() {
        return new Runnable() {
            @Override
            public void run() {
                mCount.incrementAndGet();
                sleep(500);
            }
        };
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            //
        }
    }
}

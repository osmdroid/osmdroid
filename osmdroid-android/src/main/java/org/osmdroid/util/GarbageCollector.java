package org.osmdroid.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * "Garbage Collector" tool
 * The principles are:
 * * it runs smoothly and asynchronously
 * * only one execution at the same time
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class GarbageCollector {

    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final Runnable mAction;

    public GarbageCollector(final Runnable pAction) {
        mAction = pAction;
    }

    public boolean gc() {
        if (mRunning.getAndSet(true)) {
            return false;
        }
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mAction.run();
                } finally {
                    mRunning.set(false);
                }
            }
        });
        thread.setName("GarbageCollector");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return true;
    }

    public boolean isRunning() {
        return mRunning.get();
    }
}

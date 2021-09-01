package org.osmdroid.tileprovider.modules;

import java.util.concurrent.ThreadFactory;

/**
 * @author Jastrzab
 */

public class ConfigurablePriorityThreadFactory implements ThreadFactory {

    private final int mPriority;
    private final String mName;

    public ConfigurablePriorityThreadFactory(final int pPriority, final String pName) {
        mPriority = pPriority;
        mName = pName;
    }

    @Override
    public Thread newThread(final Runnable pRunnable) {
        final Thread thread = new Thread(pRunnable);
        thread.setPriority(mPriority);
        if (mName != null) {
            thread.setName(mName);
        }
        return thread;
    }

}

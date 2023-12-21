package org.osmdroid.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    private static final String TAG = "GarbageCollector";

    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final Runnable mAction;
    @Nullable
    private WorkingThread mWorkingThread = null;
    private final Handler mHandler;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ THMAINMESSAGE_DONE })
    public @interface THMAINMESSAGE {}
    public static final int THMAINMESSAGE_DONE = -1;

    public GarbageCollector(@NonNull final Runnable pAction) {
        mAction = pAction;
        Looper cMyLooper = Looper.myLooper();
        if (cMyLooper == null) cMyLooper = Looper.getMainLooper();
        mHandler = new Handler(cMyLooper) {
            @Override
            public void handleMessage(@NonNull final Message msg) {
                @THMAINMESSAGE
                final int cWhat = msg.what;
                switch (cWhat) {
                    case THMAINMESSAGE_DONE: {
                        mRunning.set(false);
                        break;
                    }
                }
            }
        };
    }

    public boolean gc() {
        if (mRunning.getAndSet(true)) {
            return false;
        }
        /* commented out because creating a new Thread and Runnable each time is time and resource compsuming
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
        thread.setName(TAG);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        */ensureThreadIsRunning().gc();
        return true;
    }

    public boolean isRunning() {
        return mRunning.get();
    }

    private static final class WorkingThread extends Thread {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef(value={ THWORKERMESSAGE_ONSTARTED, THWORKERMESSAGE_RUN, THWORKERMESSAGE_QUIT })
        public @interface THWORKERMESSAGE {}
        public static final int THWORKERMESSAGE_ONSTARTED   = 1;
        public static final int THWORKERMESSAGE_RUN         = 2;
        public static final int THWORKERMESSAGE_QUIT        = 3;

        private final ReusablePoolDynamic.SyncObj<Boolean> mSyncObj;
        private final Runnable mRunnable;
        private Handler mWorkingHandler;
        private final Handler mExternalHandler;
        public WorkingThread(@NonNull final ReusablePoolDynamic.SyncObj<Boolean> syncObj, @NonNull final Runnable runnable, @NonNull final Handler externalHandler) {
            super(TAG);
            this.setPriority(Thread.MIN_PRIORITY);
            this.mSyncObj = syncObj;
            this.mRunnable = runnable;
            this.mExternalHandler = externalHandler;
        }
        @Override
        public void run() {
            Looper.prepare();
            //noinspection DataFlowIssue
            mWorkingHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull final Message msg) {
                    @THWORKERMESSAGE
                    final int cWhat = msg.what;
                    switch (cWhat) {
                        case THWORKERMESSAGE_ONSTARTED: {
                            synchronized (mSyncObj) {
                                mSyncObj.set(Boolean.TRUE);
                                mSyncObj.notifyAll();
                            }
                            postToThread_QuitDelayed();
                            break;
                        }
                        case THWORKERMESSAGE_RUN: {
                            try {
                                mRunnable.run();
                            } finally {
                                mExternalHandler.sendEmptyMessage(THMAINMESSAGE_DONE);
                            }
                            postToThread_QuitDelayed();
                            break;
                        }
                        case THWORKERMESSAGE_QUIT: {
                            final Looper cMyLooper = Looper.myLooper();
                            if (cMyLooper == null) break;
                            mSyncObj.set(Boolean.FALSE);
                            cMyLooper.quit();
                            break;
                        }
                    }
                }
            };
            mWorkingHandler.sendEmptyMessage(THWORKERMESSAGE_ONSTARTED);
            Looper.loop();
        }
        public boolean gc() { return this.mWorkingHandler.sendEmptyMessage(THWORKERMESSAGE_RUN); }
        private boolean postToThread_QuitDelayed() {
            this.mWorkingHandler.removeMessages(THWORKERMESSAGE_QUIT);
            return this.mWorkingHandler.sendMessageDelayed(Message.obtain(mWorkingHandler, THWORKERMESSAGE_QUIT, 0, 0, null), 5/*min*/ * 60/*sec*/ * 1000);
        }
    }

    /** @noinspection SynchronizationOnLocalVariableOrMethodParameter*/
    private WorkingThread ensureThreadIsRunning() {
        if ((mWorkingThread == null) || !mWorkingThread.isAlive() || mWorkingThread.isInterrupted()) {
            final ReusablePoolDynamic.SyncObj<Boolean> cSyncObj = new ReusablePoolDynamic.SyncObj<>(Boolean.FALSE);
            mWorkingThread = new WorkingThread(cSyncObj, mAction, mHandler);
            mWorkingThread.start();
            while (cSyncObj.get() != Boolean.TRUE) {
                try { synchronized (cSyncObj) { cSyncObj.wait(); } } catch (Throwable e) { /*nothing*/ }
            }
        }
        return mWorkingThread;
    }

}

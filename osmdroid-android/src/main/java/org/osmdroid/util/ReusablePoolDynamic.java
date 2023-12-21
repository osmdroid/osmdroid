package org.osmdroid.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import androidx.annotation.AnyThread;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ReusablePoolDynamic<I,O extends ReusablePoolDynamic.ReusableItemSetInterface<I>> {
    private static final String TAG = "ReusablePoolDynamic";

    private static final float CONST_MIN_THRESHOLD_BEFORE_START_FREEING = 0.2f;
    private static final int CONST_DEFAULT_FREE_ITEMS_DELAY_ms = 3000;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ TH_MESSAGE_FREEITEMS })
    public @interface THMESSAGE {}
    public static final int TH_MESSAGE_FREEITEMS = 1;

    private final int mInitialCapacity;
    private final ReusableIndexCallbackInternal<I> mReusablePoolCallback;
    private final ConcurrentHashMap<I, ReusableItemSetInterfaceInternal<I>> mLinkedObjects = new ConcurrentHashMap<>();
    private final SparseArray<ReusableItemSetInterfaceInternal<I>> mItems = new SparseArray<>();
    private final ConcurrentLinkedQueue<Integer> mFreeIndices = new ConcurrentLinkedQueue<>();
    private int mCapacity = 0;
    private final ConcurrentHashMap<Integer, ReusableItemSetInterfaceInternal<I>> mDeletionWithoutFreeMemoryHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ReusableItemSetInterfaceInternal<I>> mDeletionAndFreeMemoryHashMap = new ConcurrentHashMap<>();
    private int mFreeItemsDelay_ms = CONST_DEFAULT_FREE_ITEMS_DELAY_ms;
    private final Handler mHandler;
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull final Message msg) {
            @THMESSAGE
            final int cWhat = msg.what;
            switch (cWhat) {
                case TH_MESSAGE_FREEITEMS: {
                    ReusableItemSetInterfaceInternal<I> cItem;
                    int cKey;
                    for (final Map.Entry<Integer, ReusableItemSetInterfaceInternal<I>> cEntry : mDeletionWithoutFreeMemoryHashMap.entrySet()) {
                        cKey = cEntry.getKey();
                        if (cKey < mCapacity) mFreeIndices.add(cKey);
                        cItem = mDeletionWithoutFreeMemoryHashMap.remove(cKey);
                        if (cItem != null) cItem.reset();
                    }
                    for (final Map.Entry<Integer, ReusableItemSetInterfaceInternal<I>> cEntry : mDeletionAndFreeMemoryHashMap.entrySet()) {
                        cKey = cEntry.getKey();
                        if (cKey < mCapacity) mFreeIndices.add(cKey);
                        cItem = mDeletionAndFreeMemoryHashMap.remove(cKey);
                        if (cItem != null) {
                            cItem.reset();
                            cItem.freeMemory();
                        }
                    }
                    break;
                }
            }
            return false;
        }
    };

    public interface ReusableItemSetInterface<I> {
        @Nullable
        I getKey();
        void set(@NonNull I key);
        void reset();
        void freeMemory();
    }

    private static final class ReusableItemSetInterfaceInternal<I> implements ReusableItemSetInterface<I> {
        private final int mIndex;
        private final ReusableItemSetInterface<I> mItem;
        private ReusableItemSetInterfaceInternal(final int index, @NonNull final ReusableItemSetInterface<I> item) {
            this.mIndex = index;
            this.mItem = item;
        }
        public int getIndex() { return mIndex; }
        @Nullable
        @Override
        public I getKey() { return mItem.getKey(); }
        @Override
        public void set(@NonNull final I key) { mItem.set(key); }
        @Override
        public void reset() { mItem.reset(); }
        @Override
        public void freeMemory() { mItem.freeMemory(); }
    }

    private static abstract class ReusableIndexCallbackInternal<I> {
        @AnyThread public abstract ReusableItemSetInterfaceInternal<I> newInstance(int index);
        @AnyThread public abstract void onCapacityChanged(final int capacity);
    }
    public static abstract class ReusableIndexCallback<I> {
        @AnyThread public abstract ReusableItemSetInterface<I> newInstance();
        @AnyThread public void onCapacityChanged(final int capacity) { /*nothing*/ }
    }

    public ReusablePoolDynamic(@NonNull final ReusableIndexCallback<I> callback, final int initialCapacity) {
        this(callback, initialCapacity, false, null);
    }

    public ReusablePoolDynamic(@NonNull final ReusableIndexCallback<I> callback, final int initialCapacity, final int freeItemsDelay) {
        this(callback, initialCapacity, false, null, freeItemsDelay);
    }

    private ReusablePoolDynamic(@NonNull final ReusableIndexCallback<I> callback, final int initialCapacity, final boolean execInNewThread) {   //TODO: verify if it could be necessari to implement a new Thread
        this(callback, initialCapacity, execInNewThread, null);
    }

    public ReusablePoolDynamic(@NonNull final ReusableIndexCallback<I> callback, final int initialCapacity, final boolean execInNewThread, @Nullable final Handler handler) {
        this(callback, initialCapacity, execInNewThread, handler, 0);
    }

    public ReusablePoolDynamic(@NonNull final ReusableIndexCallback<I> callback, final int initialCapacity, final boolean execInNewThread, @Nullable final Handler handler, final int freeItemsDelay) {
        mReusablePoolCallback = new ReusableIndexCallbackInternal<I>() {
            @Override public ReusableItemSetInterfaceInternal<I> newInstance(final int index) { return new ReusableItemSetInterfaceInternal<>(index, callback.newInstance()); }
            @Override public void onCapacityChanged(final int capacity) { callback.onCapacityChanged(capacity); }
        };
        mInitialCapacity = initialCapacity;
        initCapacity(initialCapacity);
        mReusablePoolCallback.onCapacityChanged(initialCapacity);
        mFreeItemsDelay_ms = ((freeItemsDelay > 0) ? freeItemsDelay : CONST_DEFAULT_FREE_ITEMS_DELAY_ms);
        final Looper cLooper;
        if (handler != null) {
            cLooper = handler.getLooper();
            mHandler = new Handler(cLooper, mHandlerCallback);
        } else if ((cLooper = Looper.myLooper()) == null) {
            mHandler = new Handler(Looper.getMainLooper(), mHandlerCallback);
        } else {
            mHandler = new Handler(cLooper, mHandlerCallback);
        }
    }

    private static final class WorkerThread extends Thread {    //TODO: verify if it could be necessary to implement a new Thread
        private static final String TAG = "WorkerThread";
        public WorkerThread() {
            super(ReusablePoolDynamic.TAG+"."+TAG);
        }
        @Override
        public void run() {
            Looper.prepare();

            Looper.loop();
        }
    }

    private synchronized void initCapacity(final int initialCapacity) {
        mFreeIndices.clear();
        mCapacity = initialCapacity;
        synchronized (mItems) {
            mItems.clear();
            ReusableItemSetInterfaceInternal<I> cNewInstance;
            I cKey;
            for (int i=0; i<mCapacity; i++) {
                cNewInstance = mReusablePoolCallback.newInstance(i);
                if (cNewInstance == null) break;
                mItems.put(i, cNewInstance);
                if (cNewInstance.mItem == null) throw new IllegalStateException("Invalid Instance");
                cKey = cNewInstance.mItem.getKey();
                if ((cKey != null) && !mLinkedObjects.containsKey(cKey)) mLinkedObjects.put(cKey, cNewInstance);
                mFreeIndices.add(i);
            }
            mCapacity = mItems.size();
            mReusablePoolCallback.onCapacityChanged(mCapacity);
        }
    }

    public synchronized int getCapacity() { return mCapacity; }

    public int getAvailableCount() { return mFreeIndices.size(); }

    @AnyThread
    public <T extends ReusableItemSetInterface<I>> T getFreeItemAndSet(@NonNull final I key) {
        if (mFreeIndices.size() < (mCapacity * CONST_MIN_THRESHOLD_BEFORE_START_FREEING)) {
            final int cNewCapacity = (mCapacity * 2);
            synchronized (mItems) {
                ReusableItemSetInterfaceInternal<I> cNewInstance;
                for (int i=mCapacity; i<cNewCapacity; i++) {
                    cNewInstance = mReusablePoolCallback.newInstance(i);
                    if (cNewInstance == null) break;
                    mFreeIndices.add(i);
                    mItems.put(i, cNewInstance);
                }
            }
            mCapacity = mItems.size();
            mReusablePoolCallback.onCapacityChanged(mCapacity);
        }
        //noinspection DataFlowIssue
        final ReusableItemSetInterfaceInternal<I> res = mItems.get(mFreeIndices.poll());
        res.set(key);
        if (!mLinkedObjects.containsKey(key)) mLinkedObjects.put(key, res);
        //Log.e(TAG, "__getFirstFreeIndice(remaining: " + mFreeIndices.size());
        //noinspection unchecked
        return (T)res.mItem;
    }

    public boolean setItemElegibleToBeFreed(@Nullable final ReusableItemSetInterface<I> item, final boolean freeMemoryAtEnd) {
        if (item == null) return false;
        I cKey = item.getKey();
        if (cKey == null) {
            synchronized (mItems) {
                ReusableItemSetInterface<I> cItem;
                for (int i=0; i<mItems.size(); i++) {
                    cItem = mItems.valueAt(i);
                    if (Objects.equals(cItem, item)) {
                        cKey = item.getKey();
                        break;
                    }
                }
            }
            if (cKey == null) return false;
        }
        final ReusableItemSetInterfaceInternal<I> cReusableItemSetInterfaceInternal = mLinkedObjects.get(cKey);
        if (cReusableItemSetInterfaceInternal == null) return false;
        final int cIndex = cReusableItemSetInterfaceInternal.getIndex();
        if (freeMemoryAtEnd && !mDeletionWithoutFreeMemoryHashMap.containsKey(cIndex)) mDeletionWithoutFreeMemoryHashMap.put(cIndex, cReusableItemSetInterfaceInternal);
        else if (!mDeletionAndFreeMemoryHashMap.containsKey(cIndex)) mDeletionAndFreeMemoryHashMap.put(cIndex, cReusableItemSetInterfaceInternal);
        postponeFreeItems();
        return true;
    }

    private void postponeFreeItems() { postToThread_FreeItems(); }

    public void setFreeItemsDelay(final int delay_ms) { mFreeItemsDelay_ms = delay_ms; }

    public void freeMemory() {
        synchronized (mItems) {
            for (final ReusableItemSetInterfaceInternal<I> cItem : mDeletionWithoutFreeMemoryHashMap.values()) {
                if (cItem == null) continue;
                cItem.reset();
            }
            mDeletionWithoutFreeMemoryHashMap.clear();
            final int cSize = mItems.size();
            for (int i=(cSize-1); i>=mInitialCapacity; i--) {
                mFreeIndices.remove(i);
                mItems.remove(i);
            }
        }
        mCapacity = mInitialCapacity;
        mReusablePoolCallback.onCapacityChanged(mCapacity);
    }

    public boolean containsKey(@NonNull final I key) { return mLinkedObjects.containsKey(key); }

    private boolean postToThread(@THMESSAGE final int what) { return mHandler.sendEmptyMessage(what); }
    private boolean postToThread(@THMESSAGE final int what, final int arg1, final int arg2) { return mHandler.sendMessage(Message.obtain(mHandler, what, arg1, arg2)); }
    private boolean postToThread(@THMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj, final boolean removePrevSimilarMessages, final boolean dontDuplicatePost, final int delay) {
        if (dontDuplicatePost && mHandler.hasMessages(what)) return true;
        if (removePrevSimilarMessages) mHandler.removeMessages(what);
        final Message cMessage = Message.obtain(mHandler, what, arg1, arg2, obj);
        return ((delay <= 0) ? mHandler.sendMessage(cMessage) : mHandler.sendMessageDelayed(cMessage, delay));
    }
    private boolean postToThread_FreeItems() {
        return postToThread(TH_MESSAGE_FREEITEMS, 0, 0, null, false, true, mFreeItemsDelay_ms);
    }

    public static final class SyncObj<T> {
        private T mValue = null;
        public SyncObj() { /*nothing*/ }
        public SyncObj(@NonNull final T initValue) { this.set(initValue); }
        public void set(@NonNull final T newValue) { this.mValue = newValue; }
        @Nullable
        public T get() { return this.mValue; }
        public void clear() { this.mValue = null; }
        public void reset() { this.clear(); }
    }

}

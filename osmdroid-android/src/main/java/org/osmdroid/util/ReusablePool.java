package org.osmdroid.util;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;

public class ReusablePool<T> {

    private final Object mSyncObj = new Object();
    private final IReusablePoolItemCallback<T> mCallback;
    private int mCapacity = 0;
    private final LinkedList<T> mPool = new LinkedList<>();

    public ReusablePool(@NonNull final IReusablePoolItemCallback<T> callback, final int initCapacity) {
        mCallback = callback;
        mCapacity = initCapacity;
        this.ensureCapacity();
    }

    private void ensureCapacity() {
        synchronized (mSyncObj) {
            if ((mPool.size() == 0) || (((mPool.size() * 100) / mCapacity) < 10)) {
                final int cCurrentCapacity = mCapacity;
                mCapacity *= 2;
                if (mCapacity <= 0) mCapacity = 16;
                for (int i=cCurrentCapacity; i<mCapacity; i++) {
                    mPool.add(mCallback.newInstance());
                }
            }
        }
    }

    public T getFreeItemFromPoll() {
        synchronized (mSyncObj) {
            this.ensureCapacity();
            return mPool.poll();
        }
    }

    public void returnItemToPool(@NonNull final T item) {
        synchronized (mSyncObj) {
            mPool.add(item);
        }
    }

    public void returnItemsToPool(@NonNull final Collection<T> items) {
        synchronized (mSyncObj) {
            mPool.addAll(items);
        }
    }

    public interface IReusablePoolItemCallback<T> {
        T newInstance();
    }

}

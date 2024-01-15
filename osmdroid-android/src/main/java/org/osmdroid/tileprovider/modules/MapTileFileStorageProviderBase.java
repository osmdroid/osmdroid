package org.osmdroid.tileprovider.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.osmdroid.tileprovider.IRegisterReceiver;

import androidx.annotation.NonNull;

public abstract class MapTileFileStorageProviderBase extends MapTileModuleProviderBase {

    private final IRegisterReceiver mRegisterReceiver;
    private MyBroadcastReceiver mBroadcastReceiver;

    public MapTileFileStorageProviderBase(final Context context, final IRegisterReceiver pRegisterReceiver, final int pThreadPoolSize, final int pPendingQueueSize) {
        super(pThreadPoolSize, pPendingQueueSize);

        mRegisterReceiver = pRegisterReceiver;
        mBroadcastReceiver = new MyBroadcastReceiver();

        final IntentFilter mediaFilter = new IntentFilter();
        mediaFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mediaFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mediaFilter.addDataScheme("file");
        if (pRegisterReceiver.registerReceiver(context, mBroadcastReceiver, mediaFilter) == null) {
            mBroadcastReceiver = null;
        }
    }

    @Override
    public void detach(@NonNull final Context context) {
        if (mBroadcastReceiver != null) {
            mRegisterReceiver.unregisterReceiver(context, mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        super.detach(context);
    }

    protected void onMediaMounted(@NonNull final Context context) {
        // Do nothing by default. Override to handle.
    }

    protected void onMediaUnmounted(@NonNull final Context context) {
        // Do nothing by default. Override to handle.
    }

    /**
     * This broadcast receiver will recheck the sd card when the mount/unmount messages happen
     */
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context aContext, final Intent aIntent) {
            final String action = aIntent.getAction();
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                onMediaMounted(aContext.getApplicationContext());
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                onMediaUnmounted(aContext.getApplicationContext());
            }
        }
    }
}

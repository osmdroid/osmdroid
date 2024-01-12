package org.osmdroid.tileprovider.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import org.osmdroid.tileprovider.IRegisterReceiver;

import androidx.annotation.NonNull;

public class SimpleRegisterReceiver implements IRegisterReceiver {

    private Context mContext;

    /**
     * @deprecated Use instead: {@link #SimpleRegisterReceiver()}
     */
    @Deprecated
    public SimpleRegisterReceiver(@NonNull final Context context) {
        mContext = context;
    }
    public SimpleRegisterReceiver() { /*nothing*/ }

    @Deprecated
    @Override
    public Intent registerReceiver(final BroadcastReceiver aReceiver, final IntentFilter aFilter) {
        if (mContext == null) throw new IllegalStateException("You must instantiate this " + SimpleRegisterReceiver.class.getSimpleName() + " using the obsolete/deprecated Constructor");
        return this.registerReceiver(mContext, aReceiver, aFilter);
    }
    @Deprecated
    @Override
    public void unregisterReceiver(final BroadcastReceiver aReceiver) {
        if (mContext == null) throw new IllegalStateException("You must register this Receiver using the obsolete/deprecated registering method");
        this.unregisterReceiver(mContext, aReceiver);
    }

    @Override
    public Intent registerReceiver(@NonNull final Context context, @NonNull final BroadcastReceiver receiver, @NonNull final IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            return context.registerReceiver(receiver, filter);
        }
    }
    @Override
    public void unregisterReceiver(@NonNull final Context context, @NonNull final BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    @Override
    public void destroy() {
        mContext = null;
    }
}

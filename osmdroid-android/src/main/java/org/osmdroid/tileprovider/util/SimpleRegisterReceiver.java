package org.osmdroid.tileprovider.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import org.osmdroid.tileprovider.IRegisterReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleRegisterReceiver implements IRegisterReceiver {

    public SimpleRegisterReceiver() { /*nothing*/ }

    @Nullable
    @Override
    public Intent registerReceiver(@NonNull final Context context, @NonNull final BroadcastReceiver receiver, @NonNull final IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.getApplicationContext().registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            return context.getApplicationContext().registerReceiver(receiver, filter);
        }
    }
    @Override
    public void unregisterReceiver(@NonNull final Context context, @NonNull final BroadcastReceiver receiver) {
        context.getApplicationContext().unregisterReceiver(receiver);
    }

    @Override
    public void destroy() { /*nothing*/ }

}

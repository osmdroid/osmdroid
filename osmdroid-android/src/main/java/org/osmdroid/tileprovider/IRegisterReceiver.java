package org.osmdroid.tileprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

public interface IRegisterReceiver {

    /**
     * @deprecated Use instead: {@link #registerReceiver(Context, BroadcastReceiver, IntentFilter)}
     */
    @Deprecated
    Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
    /**
     * @deprecated Use instead: {@link #unregisterReceiver(Context, BroadcastReceiver)}
     */
    @Deprecated
    void unregisterReceiver(BroadcastReceiver receiver);

    Intent registerReceiver(@NonNull Context context, @NonNull BroadcastReceiver receiver, @NonNull IntentFilter filter);
    void unregisterReceiver(@NonNull Context context, BroadcastReceiver receiver);

    void destroy();
}

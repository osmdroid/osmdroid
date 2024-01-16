package org.osmdroid.tileprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IRegisterReceiver {

    @Nullable
    Intent registerReceiver(@NonNull Context context, @NonNull BroadcastReceiver receiver, @NonNull IntentFilter filter);
    void unregisterReceiver(@NonNull Context context, BroadcastReceiver receiver);

    void destroy();

}

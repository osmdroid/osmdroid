package org.osmdroid.tileprovider.modules;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.util.Patterns;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

public class NetworkAvailabliltyCheck implements INetworkAvailablityCheck {

    private final ConnectivityManager mConnectivityManager;
    private final PackageManager mPackageManager;
    private final String mPackageName;
    @Nullable
    private Boolean mIsNetworkAvailable = null;

    public NetworkAvailabliltyCheck(@NonNull final Context context) {
        mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mPackageManager = context.getPackageManager();
        mPackageName = context.getPackageName();
        new NetworkStateManager(new BroadcastReceiverNetwork.NetworkListener() {
            @Override
            public void OnConnected() {
                NetworkAvailabliltyCheck.this.mIsNetworkAvailable = Boolean.TRUE;
            }
            @Override
            public void OnDisconnected() {
                NetworkAvailabliltyCheck.this.mIsNetworkAvailable = Boolean.FALSE;
            }
        }).register(context);
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean getNetworkAvailable() {
        try {
            if (mIsNetworkAvailable != null) return mIsNetworkAvailable;
            return this.isNetworkAvailable();
        } catch (IllegalAccessException e) {
            return true;    // if we're unable to check network state, assume we have a network
        }
    }

    /** @noinspection deprecation*/
    @SuppressLint("MissingPermission")
    @Override
    public boolean getWiFiNetworkAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final NetworkInfo cNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return (cNetworkInfo != null) && cNetworkInfo.isConnected();
        } else {
            try {
                if (mIsNetworkAvailable != null) return mIsNetworkAvailable;
                return this.isNetworkWiFi();
            } catch (IllegalAccessException e) {
                return true;    // if we're unable to check network state, assume we have a network
            }
        }
    }

    /** @noinspection deprecation*/
    @SuppressLint("MissingPermission")
    @Override
    public boolean getCellularDataNetworkAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final NetworkInfo cNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return (cNetworkInfo != null) && cNetworkInfo.isConnected();
        } else {
            try {
                if (mIsNetworkAvailable != null) return mIsNetworkAvailable;
                return this.isNetworkMobile();
            } catch (IllegalAccessException e) {
                return true;    // if we're unable to check network state, assume we have a network
            }
        }
    }

    @Deprecated
    @Override
    public boolean getRouteToPathExists(final int hostAddress) {
        // TODO check for CHANGE_NETWORK_STATE permission
        //return mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_WIFI, hostAddress)
        //	|| mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, hostAddress);
        return true;
    }


    /** Returns <i>TRUE</i> if provided String is a valid IPv4 Address */
    public static boolean isValidIPAddress(@Nullable final String ipv4Address) {
        if ((ipv4Address == null) || ipv4Address.trim().isEmpty()) return false;
        return Patterns.IP_ADDRESS.matcher(ipv4Address).matches();
    }

    /**
     * Returns <i>TRUE</i> if there is at most one connection
     * @noinspection deprecation
     */
    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(@NonNull final Context context) throws IllegalAccessException {
        if (context.getPackageManager().checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) throw new IllegalAccessException("No NETWORK_STATE permission granted");
        final ConnectivityManager cConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo cNetworkInfo = cConnectivityManager.getActiveNetworkInfo();
        if (cNetworkInfo != null) return (cNetworkInfo.isAvailable() && cNetworkInfo.isConnected());
        return false;
    }
    /**
     * Returns <i>TRUE</i> if there is at most one connection
     * @noinspection deprecation
     */
    @SuppressLint("MissingPermission")
    public boolean isNetworkAvailable() throws IllegalAccessException {
        if (mPackageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, mPackageName) != PackageManager.PERMISSION_GRANTED) throw new IllegalAccessException("No NETWORK_STATE permission granted");
        final NetworkInfo cNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (cNetworkInfo != null) return (cNetworkInfo.isAvailable() && cNetworkInfo.isConnected());
        return false;
    }

    /** Returns <i>TRUE</i> if there is an <b>WiFi</b> connection available and connected */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isNetworkWiFi(@NonNull final Context context) throws IllegalAccessException {
        return isNetworkSpecific(context, ConnectivityManager.TYPE_WIFI);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean isNetworkWiFi() throws IllegalAccessException {
        return this.isNetworkSpecific(ConnectivityManager.TYPE_WIFI);
    }

    /** Returns <i>TRUE</i> if there is an <b>Mobile</b> connection available and connected */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isNetworkMobile(@NonNull final Context context) throws IllegalAccessException {
        return isNetworkSpecific(context, ConnectivityManager.TYPE_MOBILE);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean isNetworkMobile() throws IllegalAccessException {
        return this.isNetworkSpecific(ConnectivityManager.TYPE_MOBILE);
    }

    /** Returns <i>TRUE</i> if there is an <b>Ethernet</b> connection available and connected */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isNetworkEthernet(@NonNull final Context context) throws IllegalAccessException {
        return isNetworkSpecific(context, ConnectivityManager.TYPE_ETHERNET);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean isNetworkEthernet() throws IllegalAccessException {
        return this.isNetworkSpecific(ConnectivityManager.TYPE_ETHERNET);
    }

    /**
     * Returns <i>TRUE</i> if there is an <b>Ethernet</b> connection available and connected
     * @noinspection deprecation
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean isNetworkSpecific(@NonNull final Context context, final int connectivityManagerType) throws IllegalAccessException {
        if (context.getPackageManager().checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) throw new IllegalAccessException("No NETWORK_STATE permission granted");
        final ConnectivityManager cConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.Network[] cNetworks = cConnectivityManager.getAllNetworks();
        NetworkInfo cNetworkInfo;
        for (final android.net.Network cNetwork : cNetworks) {
            cNetworkInfo = cConnectivityManager.getNetworkInfo(cNetwork);
            if ((cNetworkInfo != null) && (cNetworkInfo.getType() == connectivityManagerType) && cNetworkInfo.isAvailable() && cNetworkInfo.isConnected()) return true;
        }
        return false;
    }
    /**
     * Returns <i>TRUE</i> if there is at most one connection
     * @noinspection deprecation
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    public boolean isNetworkSpecific(final int connectivityManagerType) throws IllegalAccessException {
        if (mPackageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, mPackageName) != PackageManager.PERMISSION_GRANTED) throw new IllegalAccessException("No NETWORK_STATE permission granted");
        final android.net.Network[] cNetworks = mConnectivityManager.getAllNetworks();
        NetworkInfo cNetworkInfo;
        for (final android.net.Network cNetwork : cNetworks) {
            cNetworkInfo = mConnectivityManager.getNetworkInfo(cNetwork);
            if ((cNetworkInfo != null) && (cNetworkInfo.getType() == connectivityManagerType) && cNetworkInfo.isAvailable() && cNetworkInfo.isConnected()) return true;
        }
        return false;
    }

    /** Runtime {@link BroadcastReceiver} for <i>Network Connectivity</i> */
    private static final class BroadcastReceiverNetwork extends BroadcastReceiver {
        @UiThread @MainThread
        public interface NetworkListener {
            @UiThread @MainThread void OnConnected();
            @UiThread @MainThread void OnDisconnected();
        }
        private final NetworkListener mNetworkListener;
        private Boolean mLastNetworkStatus = null;
        public BroadcastReceiverNetwork(@NonNull final NetworkListener listener) {
            this.mNetworkListener = listener;
        }
        @Override
        public void onReceive(@NonNull final Context context, @Nullable final Intent intent) {
            final String cAction;
            if ((intent == null) || ((cAction = intent.getAction()) == null)) return;
            //Log.e(TAG, "__onReceive(action : '"+cAction+"')");
            final boolean cIsNetworkAvailable;
            try { cIsNetworkAvailable = NetworkAvailabliltyCheck.isNetworkAvailable(context); } catch (IllegalAccessException e) { return; }
            if (Objects.equals(this.mLastNetworkStatus, cIsNetworkAvailable)) return;
            if (cIsNetworkAvailable) this.mNetworkListener.OnConnected();
            else {
                //noinspection SwitchStatementWithTooFewBranches,EnhancedSwitchMigration
                switch (cAction) {
                    case ConnectivityManager.CONNECTIVITY_ACTION: {
                        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) this.mNetworkListener.OnDisconnected();
                        break;
                    }
                }
            }
            this.mLastNetworkStatus = cIsNetworkAvailable;
        }
    }

    private static final class NetworkStateManager {
        @Nullable
        private BroadcastReceiverNetwork mBroadcastReceiverNetwork = null;
        private NetworkStateManager(@NonNull final BroadcastReceiverNetwork.NetworkListener listener) {
            if (this.mBroadcastReceiverNetwork == null) this.mBroadcastReceiverNetwork = new BroadcastReceiverNetwork(listener);
        }
        @NonNull
        private IntentFilter createBroadcastReceiverNetwork() {
            final IntentFilter cIntentFilter = new IntentFilter();
            cIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            return cIntentFilter;
        }
        private boolean register(@NonNull final Context context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.getApplicationContext().registerReceiver(this.mBroadcastReceiverNetwork, this.createBroadcastReceiverNetwork(), Context.RECEIVER_EXPORTED);
                } else {
                    context.getApplicationContext().registerReceiver(this.mBroadcastReceiverNetwork, this.createBroadcastReceiverNetwork());
                }
                return true;
            } catch (Exception e) { /*nothing*/ }
            return false;
        }
        private boolean unregister(@NonNull final Context context) {
            if (this.mBroadcastReceiverNetwork == null) return true;
            try {
                context.getApplicationContext().unregisterReceiver(this.mBroadcastReceiverNetwork);
                return true;
            } catch (Exception e) { /*nothing*/ }
            return false;
        }
    }
    /*
    @UiThread @MainThread
    public static boolean registerNetworkStateManager(@NonNull final Context context, @NonNull final NetworkStateManager networkStateManager) {
        return networkStateManager.register(context);
    }
    @UiThread @MainThread
    public static boolean unregisterNetworkStateManager(@NonNull final Context context, @Nullable final NetworkStateManager networkStateManager) {
        if (networkStateManager == null) return false;
        return networkStateManager.unregister(context);
    }
    */

    @SuppressLint({"MissingPermission", "LongLogTag"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isURLReachable(@NonNull final Context context, @NonNull final String url, final int timeoutMilliseconds) {
        final URL cURL;
        try { cURL = new URL(url); } catch (MalformedURLException e) { return false; }
        final ConnectivityManager cConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.Network[] cNetworks = cConnectivityManager.getAllNetworks();
        URLConnection cURLConnection = null;
        for (final android.net.Network cNetwork : cNetworks) {
            try {
                cURLConnection = cNetwork.openConnection(cURL);
                cURLConnection.setConnectTimeout(timeoutMilliseconds);
                cURLConnection.setReadTimeout(timeoutMilliseconds / 2);
                cURLConnection.connect();
                return true;
            } catch (IOException e) {
                Log.e("NetworkAvailabilityCheck", "[" + e.getClass().getSimpleName() + "] Unable reach '" + url + "' using '" + cNetwork + "' Network");
            } finally {
                if (cURLConnection instanceof HttpURLConnection) ((HttpURLConnection)cURLConnection).disconnect();
            }
        }
        return false;
    }

}

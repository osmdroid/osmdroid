package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * A straightforward network check implementation. NOTE: Requires
 * android.permission.ACCESS_NETWORK_STATE and android.permission.ACCESS_WIFI_STATE (?) and
 * android.permission.INTERNET (?)
 * 
 * @author Marc Kurtz
 * 
 */

public class NetworkAvailabliltyCheck implements INetworkAvailablityCheck {

	private final ConnectivityManager mConnectionManager;
     final boolean isX86;

	public NetworkAvailabliltyCheck(final Context aContext) {
		mConnectionManager = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
          isX86= "Android-x86".equalsIgnoreCase(Build.BRAND);
	}

	@Override
	public boolean getNetworkAvailable() {
		final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
		return networkInfo != null && (networkInfo.isAvailable() || (networkInfo.getType()==ConnectivityManager.TYPE_ETHERNET && isX86));
	}

	@Override
	public boolean getWiFiNetworkAvailable() {
		final NetworkInfo wifi = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi != null && wifi.isAvailable();
	}

	@Override
	public boolean getCellularDataNetworkAvailable() {
		final NetworkInfo mobile = mConnectionManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return mobile != null && mobile.isAvailable();
	}

	@Override
	public boolean getRouteToPathExists(final int hostAddress) {
		return (mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_WIFI, hostAddress) || mConnectionManager
				.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, hostAddress));
	}

}

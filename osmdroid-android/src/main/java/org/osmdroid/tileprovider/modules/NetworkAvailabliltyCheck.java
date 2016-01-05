package org.osmdroid.tileprovider.modules;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * A straightforward network check implementation.
 *
 * @author Marc Kurtz
 */

public class NetworkAvailabliltyCheck implements INetworkAvailablityCheck {

	private final ConnectivityManager mConnectionManager;
	private final boolean mIsX86;
	private final boolean mHasNetworkStatePermission;

	public NetworkAvailabliltyCheck(final Context aContext) {
		mConnectionManager = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mIsX86 = "Android-x86".equalsIgnoreCase(Build.BRAND);

		mHasNetworkStatePermission = aContext.getPackageManager()
				.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, aContext.getPackageName())
				== PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public boolean getNetworkAvailable() {
		if (!mHasNetworkStatePermission) {
			// if we're unable to check network state, assume we have a network
			return true;
		}
		final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return false;
		}
		if (networkInfo.isAvailable()) {
			return true;
		}
		return mIsX86 && networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET;
	}

	@Override
	public boolean getWiFiNetworkAvailable() {
		if (!mHasNetworkStatePermission) {
			// if we're unable to check network state, assume we have a network
			return true;
		}
		final NetworkInfo wifi = mConnectionManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi != null && wifi.isAvailable();
	}

	@Override
	public boolean getCellularDataNetworkAvailable() {
		if (!mHasNetworkStatePermission) {
			// if we're unable to check network state, assume we have a network
			return true;
		}
		final NetworkInfo mobile = mConnectionManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return mobile != null && mobile.isAvailable();
	}

	@Override
	public boolean getRouteToPathExists(final int hostAddress) {
		// TODO check for CHANGE_NETWORK_STATE permission
		return mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_WIFI, hostAddress)
			|| mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, hostAddress);
	}

}

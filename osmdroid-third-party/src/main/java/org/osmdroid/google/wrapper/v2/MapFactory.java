package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import org.osmdroid.api.IMap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

public class MapFactory {

	private MapFactory() {
	}

	public static IMap getMap(final com.google.android.gms.maps.MapView aMapView) {
		final GoogleMap map = aMapView.getMap();
		try {
			MapsInitializer.initialize(aMapView.getContext());
		} catch (final GooglePlayServicesNotAvailableException e) {
			e.printStackTrace(); // TODO logging
			return null;
		}
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getMap(final com.google.android.gms.maps.MapFragment aMapFragment) {
		final GoogleMap map = aMapFragment.getMap();
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getMap(final com.google.android.gms.maps.SupportMapFragment aSupportMapFragment) {
		final GoogleMap map = aSupportMapFragment.getMap();
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getMap(final org.osmdroid.views.MapView aMapView) {
		return new OsmdroidMapWrapper(aMapView);
	}

	public static IMap getMap(final com.google.android.maps.MapView aMapView) {
		return new GoogleV1MapWrapper(aMapView);
	}

	public static boolean isGoogleMapsV1Supported() {
		try {
			Class.forName("com.google.android.maps.MapActivity");
			return true;
		} catch (final Throwable e) {
		}
		return false;
	}

	public static boolean isGoogleMapsV2Supported(final Context aContext) {
		try {
			// first check if Google Play Services is available
			int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(aContext);
			if (resultCode == ConnectionResult.SUCCESS) {
				// then check if OpenGL ES 2.0 is available
				final ActivityManager activityManager =
						(ActivityManager) aContext.getSystemService(Context.ACTIVITY_SERVICE);
				final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
				return configurationInfo.reqGlEsVersion >= 0x20000;
			}
		} catch (Throwable e) {
		}
		return false;
	}
}

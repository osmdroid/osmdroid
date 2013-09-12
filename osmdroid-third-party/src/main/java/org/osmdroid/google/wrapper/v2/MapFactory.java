package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.maps.GoogleMap;
import org.osmdroid.api.IMap;

public class MapFactory {

	private MapFactory(){}

	public static IMap getMap(final com.google.android.gms.maps.MapView aMapView) {
		final GoogleMap map = aMapView.getMap();
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
}

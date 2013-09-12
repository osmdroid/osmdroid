package org.osmdroid.google.wrapper;

import com.google.android.gms.maps.GoogleMap;
import org.osmdroid.api.IMap;
import org.osmdroid.views.MapView;

public class MapWrapper implements IMap {

	private GoogleMap mGoogleMap;

	private MapWrapper(final GoogleMap aGoogleMap) {
		mGoogleMap = aGoogleMap;
	}

	@Override
	public void setZoom(final int aZoomLevel) {

	}

	@Override
	public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {

	}

	@Override
	public void disableMyLocation() {

	}




	// TODO maybe the stuff below here should be in a factory class

	public static IMap getInstance(final com.google.android.gms.maps.MapView aMapView) {
		final GoogleMap map = aMapView.getMap();
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getInstance(final com.google.android.gms.maps.MapFragment aMapFragment) {
		final GoogleMap map = aMapFragment.getMap();
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getInstance(final com.google.android.gms.maps.SupportMapFragment aSupportMapFragment) {
		final GoogleMap map = aSupportMapFragment.getMap();
		return map != null ? new MapWrapper(map) : null;
	}

	public static IMap getInstance(final org.osmdroid.views.MapView aMapView) {
		return new OsmdroidMapWrapper(aMapView);
	}

	public static IMap getInstance(final com.google.android.maps.MapView aMapView) {
		return new GoogleV1MapWrapper(aMapView);
	}

	private static class OsmdroidMapWrapper implements IMap {
		private final org.osmdroid.views.MapView mMapView;

		private OsmdroidMapWrapper(final MapView aMapView) {
			mMapView = aMapView;
		}

		@Override
		public void setZoom(final int aZoomLevel) {
			mMapView.getController().setZoom(aZoomLevel);
		}

		@Override
		public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
			mMapView.getController().setCenter(new org.osmdroid.util.GeoPoint(aLatitudeE6, aLongitudeE6));
		}

		@Override
		public void disableMyLocation() {
			// TODO implementation
		}
	}

	private static class GoogleV1MapWrapper implements IMap {
		private final com.google.android.maps.MapView mMapView;

		private GoogleV1MapWrapper(final com.google.android.maps.MapView aMapView) {
			mMapView = aMapView;
		}

		@Override
		public void setZoom(final int aZoomLevel) {
			mMapView.getController().setZoom(aZoomLevel);
		}

		@Override
		public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
			mMapView.getController().setCenter(new com.google.android.maps.GeoPoint(aLatitudeE6, aLongitudeE6));
		}

		@Override
		public void disableMyLocation() {
			// TODO implementation
		}
	}

}

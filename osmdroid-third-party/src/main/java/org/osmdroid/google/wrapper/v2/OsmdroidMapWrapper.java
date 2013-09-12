package org.osmdroid.google.wrapper.v2;

import org.osmdroid.api.IMap;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

class OsmdroidMapWrapper implements IMap {
	private final MapView mMapView;

	OsmdroidMapWrapper(final MapView aMapView) {
		mMapView = aMapView;
	}

	@Override
	public void setZoom(final int aZoomLevel) {
		mMapView.getController().setZoom(aZoomLevel);
	}

	@Override
	public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
		mMapView.getController().setCenter(new GeoPoint(aLatitudeE6, aLongitudeE6));
	}

	@Override
	public void disableMyLocation() {
		// TODO implementation
	}
}

package org.osmdroid.google.wrapper.v2;

import org.osmdroid.api.IMap;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

class OsmdroidMapWrapper implements IMap {
	private final MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;

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
	public void setZoomAndCenter(final int aZoomLevel, final int aLatitudeE6, final int aLongitudeE6) {
		final IMapController controller = mMapView.getController();
		controller.setZoom(aZoomLevel);
		controller.setCenter(new GeoPoint(aLatitudeE6, aLongitudeE6));
	}

	@Override
	public void setMyLocationEnabled(final boolean aEnabled) {
		if (aEnabled) {
			if (mMyLocationOverlay == null) {
				mMyLocationOverlay = new MyLocationOverlay(mMapView.getContext(), mMapView);
				mMapView.getOverlays().add(mMyLocationOverlay);
			}
			mMyLocationOverlay.enableMyLocation();
		}
		if (!aEnabled && mMyLocationOverlay != null) {
			mMyLocationOverlay.disableMyLocation();
		}
	}

	@Override
	public boolean isMyLocationEnabled() {
		return mMyLocationOverlay != null && mMyLocationOverlay.isMyLocationEnabled();
	}
}

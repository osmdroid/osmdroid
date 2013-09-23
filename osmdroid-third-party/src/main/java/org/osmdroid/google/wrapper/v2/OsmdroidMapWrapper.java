package org.osmdroid.google.wrapper.v2;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IProjection;
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
	public float getZoomLevel() {
		return mMapView.getZoomLevel();
	}

	@Override
	public void setZoom(final float aZoomLevel) {
		mMapView.getController().setZoom((int) aZoomLevel);
	}

	@Override
	public IGeoPoint getCenter() {
		return mMapView.getMapCenter();
	}

	@Override
	public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
		mMapView.getController().setCenter(new GeoPoint(aLatitudeE6, aLongitudeE6));
	}

	@Override
	public float getBearing() {
		return mMapView.getMapOrientation();
	}

	@Override
	public void setBearing(final float aBearing) {
		mMapView.setMapOrientation(aBearing);
	}

	@Override
	public void setBearingAndCenter(final float aBearing, final int aLatitudeE6, final int aLongitudeE6) {
		setBearing(aBearing);
		setCenter(aLatitudeE6, aLongitudeE6);
	}

	@Override
	public void setZoomAndCenter(final float aZoomLevel, final int aLatitudeE6, final int aLongitudeE6) {
		final IMapController controller = mMapView.getController();
		controller.setZoom((int) aZoomLevel);
		controller.setCenter(new GeoPoint(aLatitudeE6, aLongitudeE6));
	}

	@Override
	public boolean zoomIn() {
		return mMapView.getController().zoomIn();
	}

	@Override
	public boolean zoomOut() {
		return mMapView.getController().zoomOut();
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

	@Override
	public IProjection getProjection() {
		return mMapView.getProjection();
	}
}

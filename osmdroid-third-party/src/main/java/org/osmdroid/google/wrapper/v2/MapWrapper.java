package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import org.osmdroid.api.IMap;

class MapWrapper implements IMap {

	private GoogleMap mGoogleMap;

	MapWrapper(final GoogleMap aGoogleMap) {
		mGoogleMap = aGoogleMap;
	}

	@Override
	public void setZoom(final float aZoomLevel) {
		mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(aZoomLevel));
	}

	@Override
	public float getZoomLevel() {
		final CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
		return cameraPosition.zoom;
	}

	@Override
	public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
		final LatLng latLng = new LatLng(aLatitudeE6 / 1E6, aLongitudeE6 / 1E6);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	@Override
	public void setZoomAndCenter(final float aZoomLevel, final int aLatitudeE6, final int aLongitudeE6) {
		final LatLng latLng = new LatLng(aLatitudeE6 / 1E6, aLongitudeE6 / 1E6);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, aZoomLevel));
	}

	@Override
	public boolean zoomIn() {
		final CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
		final float maxZoom = mGoogleMap.getMaxZoomLevel();
		if (cameraPosition.zoom < maxZoom) {
			mGoogleMap.moveCamera(CameraUpdateFactory.zoomIn());
			return true;
		}
		return false;
	}

	@Override
	public boolean zoomOut() {
		final CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
		final float minZoom = mGoogleMap.getMinZoomLevel();
		if (cameraPosition.zoom > minZoom) {
			mGoogleMap.moveCamera(CameraUpdateFactory.zoomOut());
			return true;
		}
		return false;
	}

	@Override
	public void setMyLocationEnabled(final boolean aEnabled) {
		mGoogleMap.setMyLocationEnabled(aEnabled);
	}

	@Override
	public boolean isMyLocationEnabled() {
		return mGoogleMap.isMyLocationEnabled();
	}
}

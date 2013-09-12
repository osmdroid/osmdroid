package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import org.osmdroid.api.IMap;

class MapWrapper implements IMap {

	private GoogleMap mGoogleMap;

	MapWrapper(final GoogleMap aGoogleMap) {
		mGoogleMap = aGoogleMap;
	}

	@Override
	public void setZoom(final int aZoomLevel) {
		mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(aZoomLevel));
	}

	@Override
	public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
		final LatLng latLng = new LatLng(aLatitudeE6 / 1E6, aLongitudeE6 / 1E6);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	@Override
	public void setZoomAndCenter(final int aZoomLevel, final int aLatitudeE6, final int aLongitudeE6) {
		final LatLng latLng = new LatLng(aLatitudeE6 / 1E6, aLongitudeE6 / 1E6);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, aZoomLevel));
	}

	@Override
	public void setMyLocationEnabled(final boolean aEnabled) {
		// TODO implementation
	}

	@Override
	public boolean isMyLocationEnabled() {
		// TODO implementation
		return false;
	}
}

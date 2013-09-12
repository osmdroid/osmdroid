package org.osmdroid.google.wrapper.v2;

import com.google.android.gms.maps.GoogleMap;
import org.osmdroid.api.IMap;

class MapWrapper implements IMap {

	private GoogleMap mGoogleMap;

	MapWrapper(final GoogleMap aGoogleMap) {
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
}

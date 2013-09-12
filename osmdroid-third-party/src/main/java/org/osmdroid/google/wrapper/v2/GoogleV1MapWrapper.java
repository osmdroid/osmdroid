package org.osmdroid.google.wrapper.v2;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import org.osmdroid.api.IMap;

class GoogleV1MapWrapper implements IMap {
		private final MapView mMapView;

		GoogleV1MapWrapper(final MapView aMapView) {
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

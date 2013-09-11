package org.osmdroid.google.wrapper;

import com.google.android.gms.maps.MapView;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;

import android.content.Context;
import android.util.AttributeSet;

public class MapViewV2 implements IMapView {

	private final MapView mMapView;

	public MapViewV2(final MapView aMapView) {
		mMapView = aMapView;
	}

	public MapViewV2(final Context aContext, final AttributeSet aAttrs, final int aDefStyle) {
		this(new MapView(aContext, aAttrs, aDefStyle));
	}

	public MapViewV2(final Context aContext, final AttributeSet aAttrs) {
		this(new MapView(aContext, aAttrs));
	}

	@Override
	public IMapController getController() {
		return null;
	}

	@Override
	public IProjection getProjection() {
		return null;
	}

	@Override
	public int getZoomLevel() {
		return 0;
	}

	@Override
	public int getMaxZoomLevel() {
		return 0;
	}

	@Override
	public int getLatitudeSpan() {
		return 0;
	}

	@Override
	public int getLongitudeSpan() {
		return 0;
	}

	@Override
	public IGeoPoint getMapCenter() {
		return null;
	}

	@Override
	public IMap getMap() {
		return new IMap() {
			@Override
			public void setZoom(final int aZoomLevel) {
				// TODO
			}

			@Override
			public void setCenter(final int aLatitudeE6, final int aLongitudeE6) {
				// TODO
			}

			@Override
			public void disableMyLocation() {
				// TODO
			}
		};
	}

	@Override
	public void setBackgroundColor(final int color) {

	}
}

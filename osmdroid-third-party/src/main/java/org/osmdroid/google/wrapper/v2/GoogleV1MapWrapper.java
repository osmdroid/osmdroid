package org.osmdroid.google.wrapper.v2;

import java.util.HashMap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.api.Polyline;
import org.osmdroid.google.overlay.GoogleItemizedOverlay;
import org.osmdroid.google.overlay.GooglePolylineOverlay;
import org.osmdroid.google.wrapper.Projection;
import org.osmdroid.util.ResourceProxyImpl;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

class GoogleV1MapWrapper implements IMap {
	private final MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	private GoogleItemizedOverlay mItemizedOverlay;
	private HashMap<Long, GooglePolylineOverlay> mPolylines;
	private OnCameraChangeListener mOnCameraChangeListener;

	GoogleV1MapWrapper(final MapView aMapView) {
		mMapView = aMapView;

		mMapView.setClickable(true);
		mMapView.getOverlays().add(new Overlay() {
			@Override
			public boolean onTouchEvent(final MotionEvent aMotionEvent, final MapView aMapView) {
				if (aMotionEvent.getAction() == MotionEvent.ACTION_UP) {
					onCameraChange();
				}
				return super.onTouchEvent(aMotionEvent, aMapView);
			}
		});
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
		return new org.osmdroid.google.wrapper.GeoPoint(mMapView.getMapCenter());
	}

	@Override
	public void setCenter(final double aLatitude, final double aLongitude) {
		mMapView.getController().setCenter(new GeoPoint((int) (aLatitude * 1E6), (int) (aLongitude * 1E6)));
		onCameraChange();
	}

	@Override
	public float getBearing() {
		// TODO implementation
		return 0;
	}

	@Override
	public void setBearing(final float bearing) {
		// TODO implementation
	}

	@Override
	public void setPosition(final IPosition aPosition) {
		if (aPosition.hasBearing()) {
			setBearing(aPosition.getBearing());
		}
		if (aPosition.hasZoomLevel()) {
			setZoom(aPosition.getZoomLevel());
		}
		setCenter(aPosition.getLatitude(), aPosition.getLongitude());
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
		return new Projection(mMapView);
	}

	@Override
	public void addMarker(final Marker aMarker) {
		if (mItemizedOverlay == null) {
			mItemizedOverlay = new GoogleItemizedOverlay(new ResourceProxyImpl(mMapView.getContext()).getDrawable(ResourceProxy.bitmap.marker_default));
			mMapView.getOverlays().add(mItemizedOverlay);
		}
		final OverlayItem item = new OverlayItem(new GeoPoint((int) (aMarker.latitude * 1E6), (int) (aMarker.longitude * 1E6)), aMarker.title, aMarker.snippet);
		if (aMarker.bitmap != null || aMarker.icon != 0) {
			final Drawable drawable = aMarker.bitmap != null
			? new BitmapDrawable(mMapView.getResources(), aMarker.bitmap)
			: mMapView.getResources().getDrawable(aMarker.icon);
			if (aMarker.anchor == Marker.Anchor.CENTER) {
				mItemizedOverlay.setOverlayMarkerCentered(item, drawable);
			} else {
				item.setMarker(drawable);
		}}
		mItemizedOverlay.addOverlay(item);
	}

	@Override
	public long addPolyline(final Polyline aPolyline) {
		if (mPolylines == null) {
			mPolylines = new HashMap<Long, GooglePolylineOverlay>();
		}
		final GooglePolylineOverlay overlay = new GooglePolylineOverlay(aPolyline.color, aPolyline.width);
		for(final IGeoPoint point : aPolyline.points) {
			overlay.addPoint(point);
		}
		mMapView.getOverlays().add(overlay);
		mPolylines.put(aPolyline.id, overlay);
		return aPolyline.id;
	}

	@Override
	public void addPointToPolyline(final long id, final IGeoPoint aPoint) {
		getPolyline(id).addPoint(aPoint);
	}

	@Override
	public void clearPolyline(final long id) {
		mMapView.getOverlays().remove(getPolyline(id));
	}

	private GooglePolylineOverlay getPolyline(final long id) {
		if (mPolylines == null) {
			throw new IllegalArgumentException("No such id");
		}
		final GooglePolylineOverlay polyline = mPolylines.get(id);
		if (polyline == null) {
			throw new IllegalArgumentException("No such id");
		}
		return polyline;
	}

	@Override
	public void clear() {
		if (mItemizedOverlay != null) {
			mItemizedOverlay.removeAllItems();
		}
		if (mPolylines != null) {
			for(GooglePolylineOverlay polyline : mPolylines.values()) {
				mMapView.getOverlays().remove(mPolylines.remove(polyline));
			}
			mPolylines.clear();
		}
	}

	@Override
	public void setOnCameraChangeListener(final OnCameraChangeListener aListener) {
		mOnCameraChangeListener = aListener;
	}

	private void onCameraChange() {
		if (mOnCameraChangeListener != null) {
			mOnCameraChangeListener.onCameraChange(null); // TODO set the parameter
		}
	}
}

package org.osmdroid.google.wrapper.v2;

import java.util.ArrayList;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

class OsmdroidMapWrapper implements IMap {
	private final MapView mMapView;
	private MyLocationNewOverlay mMyLocationOverlay;
	private ItemizedOverlayWithFocus<OverlayItem> mItemizedOverlay;
	private OnCameraChangeListener mOnCameraChangeListener;

	OsmdroidMapWrapper(final MapView aMapView) {
		mMapView = aMapView;

		mMapView.getOverlays().add(new Overlay(mMapView.getContext()) {
			@Override
			protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {
				// nothing to draw
			}

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
		return mMapView.getMapCenter();
	}

	@Override
	public void setCenter(final double aLatitude, final double aLongitude) {
		mMapView.getController().setCenter(new GeoPoint(aLatitude, aLongitude));
		onCameraChange();
	}

	@Override
	public float getBearing() {
		return -mMapView.getMapOrientation();
	}

	@Override
	public void setBearing(final float aBearing) {
		mMapView.setMapOrientation(-aBearing);
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
				mMyLocationOverlay = new MyLocationNewOverlay(mMapView.getContext(), mMapView);
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

	@Override
	public void addMarker(final Marker aMarker) {
		if (mItemizedOverlay == null) {
			// XXX this is a bit cumbersome. Maybe we should just do a simple ItemizedIconOverlay with null listener
			mItemizedOverlay = new ItemizedOverlayWithFocus<OverlayItem>(new ArrayList<OverlayItem>(), new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
				@Override
				public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
					return false;
				}

				@Override
				public boolean onItemLongPress(final int index, final OverlayItem item) {
					return false;
				}
			}, new ResourceProxyImpl(mMapView.getContext()));
			mItemizedOverlay.setFocusItemsOnTap(true);
			mMapView.setUseSafeCanvas(false); // needed in case the markers are shape drawables, see issue 441
			mMapView.getOverlays().add(mItemizedOverlay);
		}
		final OverlayItem item = new OverlayItem(aMarker.title, aMarker.snippet, new GeoPoint(aMarker.latitude, aMarker.longitude));
		if (aMarker.bitmap != null) {
			item.setMarker(new BitmapDrawable(mMapView.getResources(), aMarker.bitmap));
		} else {
			if (aMarker.icon != 0) {
				item.setMarker(mMapView.getResources().getDrawable(aMarker.icon));
			}
		}
		if (aMarker.anchor == Marker.Anchor.CENTER) {
			item.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		}
		mItemizedOverlay.addItem(item);
	}

	@Override
	public void clear() {
		if (mItemizedOverlay != null) {
			mItemizedOverlay.removeAllItems();
		}
		// TODO clear everything else this is supposed to clear
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

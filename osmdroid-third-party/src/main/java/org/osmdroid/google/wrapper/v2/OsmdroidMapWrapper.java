package org.osmdroid.google.wrapper.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.api.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

class OsmdroidMapWrapper implements IMap {
	private final MapView mMapView;
	private MyLocationNewOverlay mMyLocationOverlay;
	private ItemizedOverlayWithFocus<OverlayItem> mItemizedOverlay;
	private HashMap<Integer, PathOverlay> mPolylines;
	private OnCameraChangeListener mOnCameraChangeListener;
	private static final Random random = new Random();

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
				mMyLocationOverlay = new MyLocationNewOverlay(mMapView);
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
			},this.mMapView.getContext());
			mItemizedOverlay.setFocusItemsOnTap(true);
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
	public int addPolyline(final Polyline aPolyline) {
		if (mPolylines == null) {
			mPolylines = new HashMap<Integer, PathOverlay>();
		}
		final PathOverlay overlay = new PathOverlay(aPolyline.color, aPolyline.width, mMapView.getContext());
		overlay.addPoints(aPolyline.points);
		mMapView.getOverlays().add(0, overlay); // add polyline overlay below markers, etc
		final int id = random.nextInt();
		mPolylines.put(id, overlay);
		return id;
	}

	@Override
	public void addPointsToPolyline(final int id, final IGeoPoint... aPoints) {
		getPolyline(id).addPoints(aPoints);
	}

	@Override
	public void clearPolyline(final int id) {
		final PathOverlay polyline = getPolyline(id);
		mMapView.getOverlays().remove(polyline);
		mPolylines.remove(id);
	}

	private PathOverlay getPolyline(final int id) {
		if (mPolylines == null) {
			throw new IllegalArgumentException("No such id");
		}
		final PathOverlay polyline = mPolylines.get(id);
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
			for(final PathOverlay polyline : mPolylines.values()) {
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

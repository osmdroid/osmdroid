package org.osmdroid.google.wrapper.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMap;
import org.osmdroid.api.IPosition;
import org.osmdroid.api.IProjection;
import org.osmdroid.api.Marker;
import org.osmdroid.api.OnCameraChangeListener;
import org.osmdroid.api.Polyline;

import android.text.TextUtils;

class MapWrapper implements IMap {

	private GoogleMap mGoogleMap;
	private HashMap<Integer, com.google.android.gms.maps.model.Polyline> mPolylines;
	private static final Random random = new Random();

	MapWrapper(final GoogleMap aGoogleMap) {
		mGoogleMap = aGoogleMap;
	}

	@Override
	public float getZoomLevel() {
		final CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
		return cameraPosition.zoom;
	}

	@Override
	public void setZoom(final float aZoomLevel) {
		mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(aZoomLevel));
	}

	@Override
	public IGeoPoint getCenter() {
		return new org.osmdroid.google.wrapper.v2.GeoPoint(mGoogleMap.getCameraPosition().target);
	}

	@Override
	public void setCenter(final double aLatitude, final double aLongitude) {
		final LatLng latLng = new LatLng(aLatitude, aLongitude);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	@Override
	public float getBearing() {
		return mGoogleMap.getCameraPosition().bearing;
	}

	@Override
	public void setBearing(final float aBearing) {
		final CameraPosition position = mGoogleMap.getCameraPosition();
		final CameraPosition newPosition = new CameraPosition(position.target, position.zoom, position.tilt, aBearing);
		final CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
		mGoogleMap.moveCamera(update);
	}

	@Override
	public void setPosition(final IPosition aPosition) {
		final CameraPosition position = mGoogleMap.getCameraPosition();
		final LatLng latLng = new LatLng(aPosition.getLatitude(), aPosition.getLongitude());
		final float bearing = aPosition.hasBearing() ? aPosition.getBearing() : position.bearing;
		final float zoom = aPosition.hasZoomLevel() ? aPosition.getZoomLevel() : position.zoom;
		final CameraPosition newPosition = new CameraPosition(latLng, zoom, position.tilt, bearing);
		final CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
		mGoogleMap.moveCamera(update);
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

	@Override
	public IProjection getProjection() {
		return new Projection(mGoogleMap.getProjection());
	}

	@Override
	public void addMarker(final Marker aMarker) {
		final MarkerOptions marker = new MarkerOptions();
		marker.position(new LatLng(aMarker.latitude, aMarker.longitude));
		if (!TextUtils.isEmpty(aMarker.title)) {
			marker.title(aMarker.title);
		}
		if (!TextUtils.isEmpty(aMarker.snippet)) {
			marker.snippet(aMarker.snippet);
		}
		if (aMarker.bitmap != null) {
			marker.icon(BitmapDescriptorFactory.fromBitmap(aMarker.bitmap));
		} else {
			if (aMarker.icon != 0) {
				marker.icon(BitmapDescriptorFactory.fromResource(aMarker.icon));
			}
		}
		if (aMarker.anchor == Marker.Anchor.CENTER) {
			marker.anchor(0.5f, 0.5f);
		}
		mGoogleMap.addMarker(marker);
	}

	@Override
	public int addPolyline(final Polyline aPolyline) {
		if (mPolylines == null) {
			mPolylines = new HashMap<Integer, com.google.android.gms.maps.model.Polyline>();
		}
		final PolylineOptions polylineOptions = new PolylineOptions().color(aPolyline.color).width(aPolyline.width);
		for(IGeoPoint point : aPolyline.points) {
			polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
		}
		final com.google.android.gms.maps.model.Polyline polyline = mGoogleMap.addPolyline(polylineOptions);
		final int id = random.nextInt();
		mPolylines.put(id, polyline);
		return id;
	}

	@Override
	public void addPointToPolyline(final int id, final IGeoPoint aPoint) {
		final com.google.android.gms.maps.model.Polyline polyline = getPolyline(id);
		final List<LatLng> points = polyline.getPoints();
		points.add(new LatLng(aPoint.getLatitude(), aPoint.getLongitude()));
		polyline.setPoints(points);
	}

	@Override
	public void clearPolyline(final int id) {
		getPolyline(id).setVisible(false);
	}

	private com.google.android.gms.maps.model.Polyline getPolyline(final int id) {
		if (mPolylines == null) {
			throw new IllegalArgumentException("No such id");
		}
		final com.google.android.gms.maps.model.Polyline polyline = mPolylines.get(id);
		if (polyline == null) {
			throw new IllegalArgumentException("No such id");
		}
		return polyline;
	}

	@Override
	public void clear() {
		mGoogleMap.clear();
	}

	@Override
	public void setOnCameraChangeListener(final OnCameraChangeListener aListener) {
		if (aListener == null) {
			mGoogleMap.setOnCameraChangeListener(null);
		} else {
			mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
				@Override
				public void onCameraChange(final CameraPosition aCameraPosition) {
					aListener.onCameraChange(null); // TODO set the parameter
				}
			});
		}
	}
}

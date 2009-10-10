// Created by plusminus on 22:01:11 - 29.09.2008
package org.andnav.osm.views.overlay;

import java.util.LinkedList;

import org.andnav.osm.R;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * 
 * @author Manuel Stahl
 *
 */
public class MyLocationOverlay extends OpenStreetMapViewOverlay implements LocationListener {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	protected final Paint mPaint = new Paint();
	
	protected final Bitmap DIRECTION_ARROW;
	
	protected OpenStreetMapView mMapView;
	private Context mCtx;
	private LocationManager mLocationManager;
	private boolean mMyLocationEnabled = false;
	private LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	
	protected Location mLocation;
	
	private final Matrix directionRotater = new Matrix();
	
	private final float DIRECTION_ARROW_CENTER_X;
	private final float DIRECTION_ARROW_CENTER_Y;
	private final int DIRECTION_ARROW_WIDTH;
	private final int DIRECTION_ARROW_HEIGHT;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public MyLocationOverlay(final Context ctx, final OpenStreetMapView mapView) {
		this.mCtx = ctx;
		this.mMapView = mapView;
		
		this.DIRECTION_ARROW = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.direction_arrow);
		
		this.DIRECTION_ARROW_CENTER_X = this.DIRECTION_ARROW.getWidth() / 2 - 0.5f;
		this.DIRECTION_ARROW_CENTER_Y = this.DIRECTION_ARROW.getHeight() / 2 - 0.5f;
		this.DIRECTION_ARROW_HEIGHT = this.DIRECTION_ARROW.getHeight();
		this.DIRECTION_ARROW_WIDTH = this.DIRECTION_ARROW.getWidth();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public Location getLastFix() {
		return mLocation;
	}
	
	public GeoPoint getMyLocation() {
		return TypeConverter.locationToGeoPoint(mLocation);
	}

	public boolean isMyLocationEnabled() {
		return mMyLocationEnabled;
	}
	
	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		return;
	}
	
	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView osmv) {
		if(this.mLocation != null){
			final OpenStreetMapViewProjection pj = osmv.getProjection();
			final Point screenCoords = new Point();
			pj.toPixels(TypeConverter.locationToGeoPoint(mLocation), screenCoords);
			
			
			/* Rotate the direction-Arrow according to the bearing we are driving. And draw it to the canvas. */
			this.directionRotater.setRotate(this.mLocation.getBearing(), DIRECTION_ARROW_CENTER_X , DIRECTION_ARROW_CENTER_Y);
			Bitmap rotatedDirection = Bitmap.createBitmap(DIRECTION_ARROW, 0, 0, DIRECTION_ARROW_WIDTH, DIRECTION_ARROW_HEIGHT, this.directionRotater, false); 
			c.drawBitmap(rotatedDirection, screenCoords.x - rotatedDirection.getWidth() / 2, screenCoords.y - rotatedDirection.getHeight() / 2, this.mPaint);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
		mMapView.setMapCenter(location.getLatitude(), location.getLongitude());
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}
	
	@Override
	public void onProviderEnabled(String provider) {
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(status == LocationProvider.AVAILABLE) {
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for(Runnable runnable: mRunOnFirstFix) {
						runnable.run();
					}
					mRunOnFirstFix.clear();
				}
			});
			t.run();
		}
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	public void disableMyLocation() {
		getLocationManager().removeUpdates(this);
	}
	
	public boolean enableMyLocation() {
		if (!mMyLocationEnabled) {
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			
			String provider = getLocationManager().getBestProvider(crit, true);
			try {
				getLocationManager().requestLocationUpdates(provider, 0, 0, this);
			} catch(Exception e) {
				disableMyLocation();
				return mMyLocationEnabled = false;
			}
		}
		return mMyLocationEnabled = true;
	}
	
	public boolean runOnFirstFix(Runnable runnable) {
		if(mMyLocationEnabled) {
			runnable.run();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
	}
	
	private LocationManager getLocationManager() {
		if(this.mLocationManager == null)
			this.mLocationManager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
		return this.mLocationManager; 
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

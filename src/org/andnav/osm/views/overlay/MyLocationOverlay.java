// Created by plusminus on 22:01:11 - 29.09.2008
package org.andnav.osm.views.overlay;

import java.util.LinkedList;

import org.andnav.osm.R;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapViewController;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay.Snappable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Manuel Stahl
 *
 */
public class MyLocationOverlay extends OpenStreetMapViewOverlay implements LocationListener, Snappable {

	public static final String DEBUGTAG = "OPENSTREETMAP";

	// ===========================================================
	// Constants
	// ===========================================================

	/**
	 * The time we wait after the last gps location before displaying
	 *  a non-gps location.
	 */
	private static final long GPS_WAIT_TIME = 20000; // 20 seconds

	// ===========================================================
	// Fields
	// ===========================================================
	
	protected final Paint mPaint = new Paint();
	protected final Paint mCirclePaint = new Paint();
	
	protected final Bitmap PERSON_ICON;
	protected final Bitmap DIRECTION_ARROW;
	
	protected OpenStreetMapViewController mMapController;
	private final LocationManager mLocationManager;
	private boolean mMyLocationEnabled = false;
	private LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	private final Point mMapCoords = new Point();
	
	protected Location mLocation;
	protected boolean mFollow = false;	// follow location updates
	private long mLastGps = 0; // last time we got a location from the gps provider
	
	private final Matrix directionRotater = new Matrix();
	
	/** Coordinates the feet of the person are located. */
	protected final android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24,39);

	private final float DIRECTION_ARROW_CENTER_X;
	private final float DIRECTION_ARROW_CENTER_Y;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public MyLocationOverlay(final Context ctx, final OpenStreetMapView mapView) {
		this.mLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		this.mMapController = mapView.getController();
		this.mCirclePaint.setARGB(0, 100, 100, 255);
		this.mCirclePaint.setAntiAlias(true);
		
		this.PERSON_ICON = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.person);
		this.DIRECTION_ARROW = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.direction_arrow);
		
		this.DIRECTION_ARROW_CENTER_X = this.DIRECTION_ARROW.getWidth() / 2 - 0.5f;
		this.DIRECTION_ARROW_CENTER_Y = this.DIRECTION_ARROW.getHeight() / 2 - 0.5f;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public Location getLastFix() {
		return mLocation;
	}
	
	public GeoPoint getMyLocation() {
		return new GeoPoint(mLocation);
	}

	public boolean isMyLocationEnabled() {
		return mMyLocationEnabled;
	}
	
	public boolean isLocationFollowEnabled() {
		return mFollow;
	}
	
	public void followLocation(boolean enable) {
		mFollow = enable;
	}
	
	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {}
	
	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView osmv) {
		if(this.mLocation != null) {
			final OpenStreetMapViewProjection pj = osmv.getProjection();
			pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
			final float radius = pj.metersToEquatorPixels(this.mLocation.getAccuracy());
			
			this.mCirclePaint.setAlpha(50);
			this.mCirclePaint.setStyle(Style.FILL);
			c.drawCircle(mMapCoords.x, mMapCoords.y, radius, this.mCirclePaint);
			
			this.mCirclePaint.setAlpha(150);
			this.mCirclePaint.setStyle(Style.STROKE);
			c.drawCircle(mMapCoords.x, mMapCoords.y, radius, this.mCirclePaint);
			
			float[] mtx = new float[9];
			c.getMatrix().getValues(mtx);
			
			if (mLocation.hasSpeed() && mLocation.getSpeed() > 1) {
				/* Rotate the direction-Arrow according to the bearing we are driving. And draw it to the canvas. */
				this.directionRotater.setRotate(this.mLocation.getBearing(), DIRECTION_ARROW_CENTER_X , DIRECTION_ARROW_CENTER_Y);
				this.directionRotater.postTranslate(-DIRECTION_ARROW_CENTER_X, -DIRECTION_ARROW_CENTER_Y);			
				this.directionRotater.postScale(1/mtx[Matrix.MSCALE_X], 1/mtx[Matrix.MSCALE_Y]);
				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
				c.drawBitmap(DIRECTION_ARROW, this.directionRotater, this.mPaint);
			} else {
				this.directionRotater.setTranslate(-PERSON_HOTSPOT.x, -PERSON_HOTSPOT.y);
				this.directionRotater.postScale(1/mtx[Matrix.MSCALE_X], 1/mtx[Matrix.MSCALE_Y]);
				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
				c.drawBitmap(PERSON_ICON, this.directionRotater, this.mPaint);
			}
		}
	}

	@Override
	public void onLocationChanged(final Location location) {

		// ignore temporary non-gps fix
		final long now = System.currentTimeMillis();
		if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
			mLastGps = now;
		} else {
			if (now < mLastGps + GPS_WAIT_TIME) {
				Log.d(DEBUGTAG, "Ignore temporary non-gps location");
				return;
			}
		}

		mLocation = location;
		if (mFollow)
			mMapController.animateTo(new GeoPoint(location));
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
	
	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, OpenStreetMapView mapView) {
		if(this.mLocation != null) {
			final OpenStreetMapViewProjection pj = mapView.getProjection();
			pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
			snapPoint.x = mMapCoords.x;
			snapPoint.y = mMapCoords.y;
			
			boolean snap = (x - mMapCoords.x)*(x - mMapCoords.x) + (y - mMapCoords.y)*(y - mMapCoords.y) < 64;
			return snap;
		} else {
			return false;
		}
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	public void disableMyLocation() {
		mLocationManager.removeUpdates(this);
		mMyLocationEnabled = false;
	}
	
	public boolean enableMyLocation() {
		if (!mMyLocationEnabled) {
			for (final String provider : mLocationManager.getAllProviders()) {
				mLocationManager.requestLocationUpdates(provider, 0, 0, this);
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
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

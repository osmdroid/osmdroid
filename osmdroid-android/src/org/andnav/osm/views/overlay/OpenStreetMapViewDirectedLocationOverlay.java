// Created by plusminus on 22:01:11 - 29.09.2008
package org.andnav.osm.views.overlay;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapViewDirectedLocationOverlay extends OpenStreetMapViewOverlay {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();

	protected final Bitmap DIRECTION_ARROW;

	protected GeoPoint mLocation;
	protected float mBearing;

	private final Matrix directionRotater = new Matrix();
	private final Point screenCoords = new Point();

	private final float DIRECTION_ARROW_CENTER_X;
	private final float DIRECTION_ARROW_CENTER_Y;
	private final int DIRECTION_ARROW_WIDTH;
	private final int DIRECTION_ARROW_HEIGHT;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapViewDirectedLocationOverlay(final Context ctx){
		this(ctx, new DefaultResourceProxyImpl(ctx));
	}

	public OpenStreetMapViewDirectedLocationOverlay(final Context ctx, final ResourceProxy pResourceProxy){
		super(pResourceProxy);
		this.DIRECTION_ARROW = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);

		this.DIRECTION_ARROW_CENTER_X = this.DIRECTION_ARROW.getWidth() / 2 - 0.5f;
		this.DIRECTION_ARROW_CENTER_Y = this.DIRECTION_ARROW.getHeight() / 2 - 0.5f;
		this.DIRECTION_ARROW_HEIGHT = this.DIRECTION_ARROW.getHeight();
		this.DIRECTION_ARROW_WIDTH = this.DIRECTION_ARROW.getWidth();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setLocation(final GeoPoint mp){
		this.mLocation = mp;
	}

	public void setBearing(final float aHeading){
		this.mBearing = aHeading;
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
			pj.toMapPixels(this.mLocation, screenCoords);

			/* Rotate the direction-Arrow according to the bearing we are driving. And draw it to the canvas. */
			this.directionRotater.setRotate(this.mBearing, DIRECTION_ARROW_CENTER_X , DIRECTION_ARROW_CENTER_Y);
			Bitmap rotatedDirection = Bitmap.createBitmap(DIRECTION_ARROW, 0, 0, DIRECTION_ARROW_WIDTH, DIRECTION_ARROW_HEIGHT, this.directionRotater, false);
			c.drawBitmap(rotatedDirection, screenCoords.x - rotatedDirection.getWidth() / 2, screenCoords.y - rotatedDirection.getHeight() / 2, this.mPaint);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

// Created by plusminus on 22:01:11 - 29.09.2008
package org.osmdroid.views.overlay;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class DirectedLocationOverlay extends Overlay {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();
	protected final Paint mAccuracyPaint = new Paint();

	protected final Bitmap DIRECTION_ARROW;

	protected GeoPoint mLocation;
	protected float mBearing;

	private final Matrix directionRotater = new Matrix();
	private final Point screenCoords = new Point();

	private final float DIRECTION_ARROW_CENTER_X;
	private final float DIRECTION_ARROW_CENTER_Y;
	private final int DIRECTION_ARROW_WIDTH;
	private final int DIRECTION_ARROW_HEIGHT;

	private int mAccuracy = 0;
	private boolean mShowAccuracy = true;

	// ===========================================================
	// Constructors
	// ===========================================================

	public DirectedLocationOverlay(final Context ctx) {
		this(ctx, new DefaultResourceProxyImpl(ctx));
	}

	public DirectedLocationOverlay(final Context ctx,
			final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		this.DIRECTION_ARROW = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);

		this.DIRECTION_ARROW_CENTER_X = this.DIRECTION_ARROW.getWidth() / 2 - 0.5f;
		this.DIRECTION_ARROW_CENTER_Y = this.DIRECTION_ARROW.getHeight() / 2 - 0.5f;
		this.DIRECTION_ARROW_HEIGHT = this.DIRECTION_ARROW.getHeight();
		this.DIRECTION_ARROW_WIDTH = this.DIRECTION_ARROW.getWidth();

		this.mAccuracyPaint.setStrokeWidth(2);
		this.mAccuracyPaint.setColor(Color.BLUE);
		this.mAccuracyPaint.setAntiAlias(true);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setShowAccuracy(final boolean pShowIt) {
		this.mShowAccuracy = pShowIt;
	}

	public void setLocation(final GeoPoint mp) {
		this.mLocation = mp;
	}

	public GeoPoint getLocation() {
		return this.mLocation;
	}

	/**
	 *
	 * @param pAccuracy
	 *            in Meters
	 */
	public void setAccuracy(final int pAccuracy) {
		this.mAccuracy = pAccuracy;
	}

	public void setBearing(final float aHeading) {
		this.mBearing = aHeading;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void draw(final Canvas c, final MapView osmv, final boolean shadow) {

		if (shadow) {
			return;
		}

		if (this.mLocation != null) {
			final Projection pj = osmv.getProjection();
			pj.toMapPixels(this.mLocation, screenCoords);

			if (this.mShowAccuracy && this.mAccuracy > 10) {
				final float accuracyRadius = pj.metersToEquatorPixels(this.mAccuracy);
				/* Only draw if the DirectionArrow doesn't cover it. */
				if (accuracyRadius > 8) {
					/* Draw the inner shadow. */
					this.mAccuracyPaint.setAntiAlias(false);
					this.mAccuracyPaint.setAlpha(30);
					this.mAccuracyPaint.setStyle(Style.FILL);
					c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius,
							this.mAccuracyPaint);

					/* Draw the edge. */
					this.mAccuracyPaint.setAntiAlias(true);
					this.mAccuracyPaint.setAlpha(150);
					this.mAccuracyPaint.setStyle(Style.STROKE);
					c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius,
							this.mAccuracyPaint);
				}
			}

			/*
			 * Rotate the direction-Arrow according to the bearing we are driving. And draw it to
			 * the canvas.
			 */
			this.directionRotater.setRotate(this.mBearing, DIRECTION_ARROW_CENTER_X,
					DIRECTION_ARROW_CENTER_Y);
			final Bitmap rotatedDirection = Bitmap.createBitmap(DIRECTION_ARROW, 0, 0,
					DIRECTION_ARROW_WIDTH, DIRECTION_ARROW_HEIGHT, this.directionRotater, false);
			c.drawBitmap(rotatedDirection, screenCoords.x - rotatedDirection.getWidth() / 2,
					screenCoords.y - rotatedDirection.getHeight() / 2, this.mPaint);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

// Created by plusminus on 22:01:11 - 29.09.2008
package org.osmdroid.views.overlay.mylocation;

import org.osmdroid.library.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class SimpleLocationOverlay extends Overlay {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();

	protected Bitmap PERSON_ICON;
	/** Coordinates the feet of the person are located. */
	protected android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24, 39);

	protected GeoPoint mLocation;
	private final Point screenCoords = new Point();

	// ===========================================================
	// Constructors
	// ===========================================================

	public SimpleLocationOverlay(final Context ctx) {
		super(ctx);
		this.PERSON_ICON = ((BitmapDrawable)ctx.getResources().getDrawable(R.drawable.person)).getBitmap();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setLocation(final GeoPoint mp) {
		this.mLocation = mp;
	}

	public GeoPoint getMyLocation() {
		return this.mLocation;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		if (!shadow && this.mLocation != null) {
			final Projection pj = osmv.getProjection();
			pj.toPixels(this.mLocation, screenCoords);

			c.drawBitmap(PERSON_ICON, screenCoords.x - PERSON_HOTSPOT.x, screenCoords.y
					- PERSON_HOTSPOT.y, this.mPaint);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/** Coordinates the feet of the person are located. */
	public void setPersonIcon(Bitmap bmp, Point hotspot){
		this.PERSON_ICON=bmp;
		this.PERSON_HOTSPOT=hotspot;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

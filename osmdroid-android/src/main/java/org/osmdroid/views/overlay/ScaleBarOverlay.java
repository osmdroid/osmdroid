package org.osmdroid.views.overlay;

/**
 * ScaleBarOverlay.java
 *
 * Puts a scale bar in the top-left corner of the screen, offset by a configurable
 * number of pixels. The bar is scaled to 1-inch length by querying for the physical
 * DPI of the screen. The size of the bar is printed between the tick marks. A
 * vertical (longitude) scale can be enabled. Scale is printed in metric (kilometers,
 * meters), imperial (miles, feet) and nautical (nautical miles, feet).
 *
 * Author: Erik Burrows, Griffin Systems LLC
 * 		erik@griffinsystems.org
 *
 * Change Log:
 * 		2010-10-08: Inclusion to osmdroid trunk
 * 		2015-12-17: Allow for top, bottom, left or right placement by W.  Strickling
 *
 * Usage: 
 * <code>
 * MapView map = new MapView(...);
 * ScaleBarOverlay scaleBar = new ScaleBarOverlay(map); // Thiw is an important change of calling!
 *
 * map.getOverlays().add(scaleBar);
 * </code>
 *
 * To Do List:
 * 1. Allow for top, bottom, left or right placement. // done in this changement
 * 2. Scale bar to precise displayed scale text after rounding.
 *
 */

import java.lang.reflect.Field;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.library.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScaleBarOverlay extends Overlay implements GeoConstants {

	// ===========================================================
	// Fields
	// ===========================================================

	private static final Rect sTextBoundsRect = new Rect();

	public enum UnitsOfMeasure {
		metric, imperial, nautical
	}

	// Defaults
	int xOffset = 10;
	int yOffset = 10;
	int minZoom = 0;

	UnitsOfMeasure unitsOfMeasure = UnitsOfMeasure.metric;

	boolean latitudeBar = true;
	boolean longitudeBar = false;
	
	protected boolean alignBottom = false;
	protected boolean alignRight  = false;


	// Internal

	private final Context context;
	private final MapView mMapView;

	protected final Path barPath = new Path();
	protected final Rect latitudeBarRect = new Rect();
	protected final Rect longitudeBarRect = new Rect();

	private int lastZoomLevel = -1;
	private float lastLatitude = 0;

	public float xdpi;
	public float ydpi;
	public int screenWidth;
	public int screenHeight;

	private Paint barPaint;
	private Paint bgPaint;
	private Paint textPaint;

	private boolean centred = false;
	private boolean adjustLength = false;
	private float maxLength;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ScaleBarOverlay(final MapView mapView) {
		super(mapView.getContext());
		this.mMapView = mapView;
		this.context = mapView.getContext();
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();

		this.barPaint = new Paint();
		this.barPaint.setColor(Color.BLACK);
		this.barPaint.setAntiAlias(true);
		this.barPaint.setStyle(Style.STROKE);
		this.barPaint.setAlpha(255);
		this.barPaint.setStrokeWidth(2 * dm.density);
		this.bgPaint = null;

		this.textPaint = new Paint();
		this.textPaint.setColor(Color.BLACK);
		this.textPaint.setAntiAlias(true);
		this.textPaint.setStyle(Style.FILL);
		this.textPaint.setAlpha(255);
		this.textPaint.setTextSize(10 * dm.density);

		this.xdpi = dm.xdpi;
		this.ydpi = dm.ydpi;

		this.screenWidth = dm.widthPixels;
		this.screenHeight = dm.heightPixels;

		// DPI corrections for specific models
		String manufacturer = null;
		try {
			final Field field = android.os.Build.class.getField("MANUFACTURER");
			manufacturer = (String) field.get(null);
		} catch (final Exception ignore) {
		}

		if ("motorola".equals(manufacturer) && "DROIDX".equals(android.os.Build.MODEL)) {

			// If the screen is rotated, flip the x and y dpi values
			WindowManager windowManager = (WindowManager) this.context
					.getSystemService(Context.WINDOW_SERVICE);
			if (windowManager.getDefaultDisplay().getOrientation() > 0) {
				this.xdpi = (float) (this.screenWidth / 3.75);
				this.ydpi = (float) (this.screenHeight / 2.1);
			} else {
				this.xdpi = (float) (this.screenWidth / 2.1);
				this.ydpi = (float) (this.screenHeight / 3.75);
			}

		} else if ("motorola".equals(manufacturer) && "Droid".equals(android.os.Build.MODEL)) {
			// http://www.mail-archive.com/android-developers@googlegroups.com/msg109497.html
			this.xdpi = 264;
			this.ydpi = 264;
		}

		// set default max length to 1 inch
		maxLength = 2.54f;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * Sets the minimum zoom level for the scale bar to be drawn.
	 * 
	 * @param zoom
	 *            minimum zoom level
	 */
	public void setMinZoom(final int zoom) {
		this.minZoom = zoom;
	}

	/**
	 * Sets the scale bar screen offset for the bar. Note: if the bar is set to be drawn centered,
	 * this will be the middle of the bar, otherwise the top left corner.
	 * 
	 * @param x
	 *            x screen offset
	 * @param y
	 *            z screen offset
	 */
	public void setScaleBarOffset(final int x, final int y) {
		xOffset = x;
		yOffset = y;
	}

	/**
	 * Sets the bar's line width. (the default is 2)
	 * 
	 * @param width
	 *            the new line width
	 */
	public void setLineWidth(final float width) {
		this.barPaint.setStrokeWidth(width);
	}

	/**
	 * Sets the text size. (the default is 12)
	 * 
	 * @param size
	 *            the new text size
	 */
	public void setTextSize(final float size) {
		this.textPaint.setTextSize(size);
	}

	/**
	 * Sets the units of measure to be shown in the scale bar
	 */
	public void setUnitsOfMeasure(UnitsOfMeasure unitsOfMeasure) {
		this.unitsOfMeasure = unitsOfMeasure;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Gets the units of measure to be shown in the scale bar
	 */
	public UnitsOfMeasure getUnitsOfMeasure() {
		return unitsOfMeasure;
	}

	/**
	 * Latitudinal / horizontal scale bar flag
	 * 
	 * @param latitude
	 */
	public void drawLatitudeScale(final boolean latitude) {
		this.latitudeBar = latitude;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Longitudinal / vertical scale bar flag
	 * 
	 * @param longitude
	 */
	public void drawLongitudeScale(final boolean longitude) {
		this.longitudeBar = longitude;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Flag to draw the bar centered around the set offset coordinates or to the right/bottom of the
	 * coordinates (default)
	 * 
	 * @param centred
	 *            set true to centre the bar around the given screen coordinates
	 */
public void setCentred(final boolean centred) {
    this.centred = centred;
    alignBottom  = !centred;
    alignRight   = !centred;
    lastZoomLevel = -1; // Force redraw of scalebar
}

public void setAlignBottom(final boolean alignBottom) {
    this.centred = false;
    this.alignBottom  = alignBottom;
    lastZoomLevel = -1; // Force redraw of scalebar
}

public void setAlignRight(final boolean alignRight) {
    this.centred = false;
    this.alignRight  = alignRight;
    lastZoomLevel = -1; // Force redraw of scalebar
}
	/**
	 * Return's the paint used to draw the bar
	 * 
	 * @return the paint used to draw the bar
	 */
	public Paint getBarPaint() {
		return barPaint;
	}

	/**
	 * Sets the paint for drawing the bar
	 * 
	 * @param pBarPaint
	 *            bar drawing paint
	 */
	public void setBarPaint(final Paint pBarPaint) {
		if (pBarPaint == null) {
			throw new IllegalArgumentException("pBarPaint argument cannot be null");
		}
		barPaint = pBarPaint;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Returns the paint used to draw the text
	 * 
	 * @return the paint used to draw the text
	 */
	public Paint getTextPaint() {
		return textPaint;
	}

	/**
	 * Sets the paint for drawing the text
	 * 
	 * @param pTextPaint
	 *            text drawing paint
	 */
	public void setTextPaint(final Paint pTextPaint) {
		if (pTextPaint == null) {
			throw new IllegalArgumentException("pTextPaint argument cannot be null");
		}
		textPaint = pTextPaint;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Sets the background paint. Set to null to disable drawing of background (default)
	 * 
	 * @param pBgPaint
	 *            the paint for colouring the bar background
	 */
	public void setBackgroundPaint(final Paint pBgPaint) {
		bgPaint = pBgPaint;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * If enabled, the bar will automatically adjust the length to reflect a round number (starting
	 * with 1, 2 or 5). If disabled, the bar will always be drawn in full length representing a
	 * fractional distance.
	 */
	public void setEnableAdjustLength(boolean adjustLength) {
		this.adjustLength = adjustLength;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Sets the maximum bar length. If adjustLength is disabled this will match exactly the length
	 * of the bar. If adjustLength is enabled, the bar will be shortened to reflect a round number
	 * in length.
	 * 
	 * @param pMaxLengthInCm
	 *            maximum length of the bar in the screen in cm. Default is 2.54 (=1 inch)
	 */
	public void setMaxLength(final float pMaxLengthInCm) {
		this.maxLength = pMaxLengthInCm;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void draw(Canvas c, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}

		// If map view is animating, don't update, scale will be wrong.
		if (mapView.isAnimating()) {
			return;
		}

		final int zoomLevel = mapView.getZoomLevel();

		if (zoomLevel >= minZoom) {
			final Projection projection = mapView.getProjection();

			if (projection == null) {
				return;
			}

			screenWidth	   = mapView.getWidth();
			screenHeight   = mapView.getHeight();
			final IGeoPoint center = projection.fromPixels(screenWidth / 2, screenHeight / 2, null);
			if (zoomLevel != lastZoomLevel
					|| (int) (center.getLatitudeE6() / 1E6) != (int) (lastLatitude / 1E6)) {
				lastZoomLevel = zoomLevel;
				lastLatitude = center.getLatitudeE6();
				rebuildBarPath(projection);
			}

			int offsetX = (int) xOffset;
			int offsetY = (int) yOffset;
			if (alignBottom) offsetY *=-1;
			if (alignRight ) offsetX *=-1;
			if (centred && latitudeBar)
				offsetX += -latitudeBarRect.width() / 2;
			if (centred && longitudeBar)
				offsetY += -longitudeBarRect.height() / 2;

			c.save();
			c.concat(projection.getInvertedScaleRotateCanvasMatrix());
			c.translate(offsetX, offsetY);

			if (latitudeBar && bgPaint != null)
				c.drawRect(latitudeBarRect, bgPaint);
			if (longitudeBar && bgPaint != null) {
				// Don't draw on top of latitude background...
				int offsetTop = latitudeBar ? latitudeBarRect.height() : 0;
				c.drawRect(longitudeBarRect.left, longitudeBarRect.top + offsetTop,
						longitudeBarRect.right, longitudeBarRect.bottom, bgPaint);
			}
			c.drawPath(barPath, barPaint);
			if (latitudeBar) {
				drawLatitudeText(c, projection);
			}
			if (longitudeBar) {
				drawLongitudeText(c, projection);
			}
			c.restore();
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void disableScaleBar() {
		setEnabled(false);
	}

	public void enableScaleBar() {
		setEnabled(true);
	}

	private void drawLatitudeText(final Canvas canvas, final Projection projection) {
		// calculate dots per centimeter
		int xdpcm = (int) ((float) xdpi / 2.54);

		// get length in pixel
		int xLen = (int) (maxLength * xdpcm);

		// Two points, xLen apart, at scale bar screen location
		IGeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xLen / 2), yOffset, null);
		IGeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xLen / 2), yOffset, null);

		// get distance in meters between points
		final int xMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double xMetersAdjusted = this.adjustLength ? adjustScaleBarLength(xMeters) : xMeters;
		// get adjusted length in pixels
		final int xBarLengthPixels = (int) (xLen * xMetersAdjusted / xMeters);

		// create text
		final String xMsg = scaleBarLengthText((int) xMetersAdjusted);
		textPaint.getTextBounds(xMsg, 0, xMsg.length(), sTextBoundsRect);
		final int xTextSpacing = (int) (sTextBoundsRect.height() / 5.0);

		float x = xBarLengthPixels / 2 - sTextBoundsRect.width() / 2;
		if (alignRight)  x+= screenWidth -xBarLengthPixels;
		float y; 
		if (alignBottom) { y = screenHeight   -xTextSpacing*2; }
		else y = sTextBoundsRect.height() + xTextSpacing;
		canvas.drawText(xMsg, x, y, textPaint);
	}

	private void drawLongitudeText(final Canvas canvas, final Projection projection) {
		// calculate dots per centimeter
		int ydpcm = (int) ((float) ydpi / 2.54);

		// get length in pixel
		int yLen = (int) (maxLength * ydpcm);

		// Two points, yLen apart, at scale bar screen location
		IGeoPoint p1 = projection
				.fromPixels(screenWidth / 2, (screenHeight / 2) - (yLen / 2), null);
		IGeoPoint p2 = projection
				.fromPixels(screenWidth / 2, (screenHeight / 2) + (yLen / 2), null);

		// get distance in meters between points
		final int yMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double yMetersAdjusted = this.adjustLength ? adjustScaleBarLength(yMeters) : yMeters;
		// get adjusted length in pixels
		final int yBarLengthPixels = (int) (yLen * yMetersAdjusted / yMeters);

		// create text
		final String yMsg = scaleBarLengthText((int) yMetersAdjusted);
		textPaint.getTextBounds(yMsg, 0, yMsg.length(), sTextBoundsRect);
		final int yTextSpacing = (int) (sTextBoundsRect.height() / 5.0);

		float x; 
		if (alignRight)  {x = screenWidth  -yTextSpacing*2;}
		else x = sTextBoundsRect.height() + yTextSpacing;
		float y = yBarLengthPixels / 2 + sTextBoundsRect.width() / 2;
		if (alignBottom) y+= screenHeight -yBarLengthPixels;
		canvas.save();
		canvas.rotate(-90, x, y);
		canvas.drawText(yMsg, x, y, textPaint);
		canvas.restore();
	}

	protected void rebuildBarPath(final Projection projection) {   //** modified to protected
		// We want the scale bar to be as long as the closest round-number miles/kilometers
		// to 1-inch at the latitude at the current center of the screen.

		// calculate dots per centimeter
		int xdpcm = (int) ((float) xdpi / 2.54);
		int ydpcm = (int) ((float) ydpi / 2.54);

		// get length in pixel
		int xLen = (int) (maxLength * xdpcm);
		int yLen = (int) (maxLength * ydpcm);

		// Two points, xLen apart, at scale bar screen location
		IGeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xLen / 2), yOffset, null);
		IGeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xLen / 2), yOffset, null);

		// get distance in meters between points
		final int xMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double xMetersAdjusted = this.adjustLength ? adjustScaleBarLength(xMeters) : xMeters;
		// get adjusted length in pixels
		final int xBarLengthPixels = (int) (xLen * xMetersAdjusted / xMeters);

		// Two points, yLen apart, at scale bar screen location
		p1 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) - (yLen / 2), null);
		p2 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) + (yLen / 2), null);

		// get distance in meters between points
		final int yMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double yMetersAdjusted = this.adjustLength ? adjustScaleBarLength(yMeters) : yMeters;
		// get adjusted length in pixels
		final int yBarLengthPixels = (int) (yLen * yMetersAdjusted / yMeters);

		// create text
		final String xMsg = scaleBarLengthText((int) xMetersAdjusted);
		final Rect xTextRect = new Rect();
		textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);
		int xTextSpacing = (int) (xTextRect.height() / 5.0);           

		// create text
		final String yMsg = scaleBarLengthText((int) yMetersAdjusted);
		final Rect yTextRect = new Rect();
		textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);
		int yTextSpacing = (int) (yTextRect.height() / 5.0);
		int xTextHeight  = xTextRect.height();
		int yTextHeight  = yTextRect.height();

		barPath.rewind();
		
		//** alignBottom ad-ons
		int barOriginX = 0;
		int barOriginY = 0;
		int barToX     = xBarLengthPixels;
		int barToY	   = yBarLengthPixels;
		if (alignBottom) {
			xTextSpacing *= -1;
			xTextHeight  *= -1;
			barOriginY   = mMapView.getHeight();
			barToY       = barOriginY -yBarLengthPixels;
		}
				
		if (alignRight) {
			yTextSpacing *= -1;
			yTextHeight  *= -1;		
			barOriginX   = mMapView.getWidth();
			barToX       = barOriginX -xBarLengthPixels;    
		}
		
		if (latitudeBar) {
			// draw latitude bar
			barPath.moveTo(barToX, barOriginY +xTextHeight + xTextSpacing * 2);
			barPath.lineTo(barToX, barOriginY);
			barPath.lineTo(barOriginX, barOriginY);

			if (!longitudeBar) {
				barPath.lineTo(barOriginX,  barOriginY +xTextHeight + xTextSpacing * 2);
			}
			latitudeBarRect.set(barOriginX, barOriginY, barToX, barOriginY +xTextHeight + xTextSpacing * 2);
		}

		if (longitudeBar) {
			// draw longitude bar
			if (!latitudeBar) {
				barPath.moveTo(barOriginX +yTextHeight + yTextSpacing * 2, barOriginY);
				barPath.lineTo(barOriginX, barOriginY);
			}

			barPath.lineTo(barOriginX, barToY);
			barPath.lineTo(barOriginX +yTextHeight + yTextSpacing * 2, barToY);

			longitudeBarRect.set(barOriginX, barOriginY, barOriginX +yTextHeight + yTextSpacing * 2, barToY);
		}
	}

	/**
	 * Returns a reduced length that starts with 1, 2 or 5 and trailing zeros. If set to nautical or
	 * imperial the input will be transformed before and after the reduction so that the result
	 * holds in that respective unit.
	 * 
	 * @param length
	 *            length to round
	 * @return reduced, rounded (in m, nm or mi depending on setting) result
	 */
	private double adjustScaleBarLength(double length) {
		long pow = 0;
		boolean feet = false;
		if (unitsOfMeasure == UnitsOfMeasure.imperial) {
			if (length >= GeoConstants.METERS_PER_STATUTE_MILE / 5)
				length = length / GeoConstants.METERS_PER_STATUTE_MILE;
			else {
				length = length * GeoConstants.FEET_PER_METER;
				feet = true;
			}
		} else if (unitsOfMeasure == UnitsOfMeasure.nautical) {
			if (length >= GeoConstants.METERS_PER_NAUTICAL_MILE / 5)
				length = length / GeoConstants.METERS_PER_NAUTICAL_MILE;
			else {
				length = length * GeoConstants.FEET_PER_METER;
				feet = true;
			}
		}

		while (length >= 10) {
			pow++;
			length /= 10;
		}
		while (length < 1 && length > 0) {
			pow--;
			length *= 10;
		}

		if (length < 2) {
			length = 1;
		} else if (length < 5) {
			length = 2;
		} else {
			length = 5;
		}
		if (feet)
			length = length / GeoConstants.FEET_PER_METER;
		else if (unitsOfMeasure == UnitsOfMeasure.imperial)
			length = length * GeoConstants.METERS_PER_STATUTE_MILE;
		else if (unitsOfMeasure == UnitsOfMeasure.nautical)
			length = length * GeoConstants.METERS_PER_NAUTICAL_MILE;
		length *= Math.pow(10, pow);
		return length;
	}

	protected String scaleBarLengthText(final int meters) {
		switch (unitsOfMeasure) {
		default:
		case metric:
			if (meters >= 1000 * 5) {
				return context.getResources().getString(R.string.format_distance_kilometers,(meters / 1000));
			} else if (meters >= 1000 / 5) {
				return context.getResources().getString(R.string.format_distance_kilometers,
						(int) (meters / 100.0) / 10.0);
			} else {
				return context.getResources().getString(R.string.format_distance_meters, meters);
			}
		case imperial:
			if (meters >= METERS_PER_STATUTE_MILE * 5) {
				return context.getResources().getString(R.string.format_distance_miles,
						(int) (meters / METERS_PER_STATUTE_MILE));

			} else if (meters >= METERS_PER_STATUTE_MILE / 5) {
				return context.getResources().getString(R.string.format_distance_miles,
						((int) (meters / (METERS_PER_STATUTE_MILE / 10.0))) / 10.0);
			} else {
				return context.getResources().getString(R.string.format_distance_feet,
						(int) (meters * FEET_PER_METER));
			}
		case nautical:
			if (meters >= METERS_PER_NAUTICAL_MILE * 5) {
				return context.getResources().getString(R.string.format_distance_nautical_miles,
						((int) (meters / METERS_PER_NAUTICAL_MILE)));
			} else if (meters >= METERS_PER_NAUTICAL_MILE / 5) {
				return context.getResources().getString(R.string.format_distance_nautical_miles,
						(((int) (meters / (METERS_PER_NAUTICAL_MILE / 10.0))) / 10.0));
			} else {
				return context.getResources().getString(R.string.format_distance_feet,
						((int) (meters * FEET_PER_METER)));
			}
		}
	}

}



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
 *
 * License:
 * 		LGPL version 3
 * 		http://www.gnu.org/licenses/lgpl.html
 *
 * Usage:
 * <code>
 * MapView map = new MapView(...);
 * ScaleBarOverlay scaleBar = new ScaleBarOverlay(this.getBaseContext(), map);
 *
 * scaleBar.setImperial(); // Metric by default
 *
 * map.getOverlays().add(scaleBar);
 * </code>
 *
 * To Do List:
 * 1. Allow for top, bottom, left or right placement.
 * 2. Scale bar to precise displayed scale text after rounding.
 *
 */

import java.lang.reflect.Field;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Picture;
import android.graphics.Rect;
import android.view.WindowManager;

public class ScaleBarOverlay extends SafeDrawOverlay implements GeoConstants {

	// ===========================================================
	// Fields
	// ===========================================================

	// Defaults

	float xOffset = 10;
	float yOffset = 10;
	float lineWidth = 2;
	final int textSize = 12;
	int minZoom = 0;

	boolean imperial = false;
	boolean nautical = false;

	boolean latitudeBar = true;
	boolean longitudeBar = false;

	// Internal

	private final Context context;

	protected final Picture scaleBarPicture = new Picture();

	private int lastZoomLevel = -1;
	private float lastLatitude = 0;

	public float xdpi;
	public float ydpi;
	public int screenWidth;
	public int screenHeight;

	private final ResourceProxy resourceProxy;
	private Paint barPaint;
	private Paint bgPaint;
	private Paint textPaint;
	private Projection projection;

	final private Rect mBounds = new Rect();
	final private Matrix mIdentityMatrix = new Matrix();

	private boolean centred = false;
	private boolean adjustLength = false;
	private float maxLength;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ScaleBarOverlay(final Context context) {
		this(context, new DefaultResourceProxyImpl(context));
	}

	public ScaleBarOverlay(final Context context, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		this.resourceProxy = pResourceProxy;
		this.context = context;

		this.barPaint = new Paint();
		this.barPaint.setColor(Color.BLACK);
		this.barPaint.setAntiAlias(true);
		this.barPaint.setStyle(Style.FILL);
		this.barPaint.setAlpha(255);
		this.bgPaint = null;

		this.textPaint = new Paint();
		this.textPaint.setColor(Color.BLACK);
		this.textPaint.setAntiAlias(true);
		this.textPaint.setStyle(Style.FILL);
		this.textPaint.setAlpha(255);
		this.textPaint.setTextSize(textSize);

		this.xdpi = this.context.getResources().getDisplayMetrics().xdpi;
		this.ydpi = this.context.getResources().getDisplayMetrics().ydpi;

		this.screenWidth = this.context.getResources().getDisplayMetrics().widthPixels;
		this.screenHeight = this.context.getResources().getDisplayMetrics().heightPixels;

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
	 * @param minimum zoom level
	 */
	public void setMinZoom(final int zoom) {
		this.minZoom = zoom;
	}

	/**
	 * Sets the scale bar screen offset for the bar. Note: if the bar is set to be drawn centered, this will be the middle of the bar, otherwise the top left corner.   
	 * @param x x screen offset
	 * @param y z screen offset
	 */
	public void setScaleBarOffset(final float x, final float y) {
		xOffset = x;
		yOffset = y;
	}

	/**
	 * Sets the bar's line width. (the default is 2)
	 * @param width the new line width
	 */
	public void setLineWidth(final float width) {
		this.lineWidth = width;
	}

	/**
	 * Sets the text size. (the default is 12)
	 * @param size the new text size
	 */
	public void setTextSize(final float size) {
		this.textPaint.setTextSize(size);
	}

	/**
	 * Sets the length to be shown in imperial units (mi/ft)
	 */
	public void setImperial() {
		this.imperial = true;
		this.nautical = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Sets the length to be shown in nautical units (nm/ft)
	 */
	public void setNautical() {
		this.nautical = true;
		this.imperial = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Sets the length to be shown in metric units (km/m)
	 */
	public void setMetric() {
		this.nautical = false;
		this.imperial = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Latitudinal / horizontal scale bar flag
	 * @param latitude
	 */
	public void drawLatitudeScale(final boolean latitude) {
		this.latitudeBar = latitude;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Longitudinal / vertical scale bar flag
	 * @param longitude
	 */
	public void drawLongitudeScale(final boolean longitude) {
		this.longitudeBar = longitude;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Flag to draw the bar centered around the set offset coordinates or to the right/bottom of the coordinates (default)
	 * @param centred set true to centre the bar around the given screen coordinates
	 */
	public void setCentred(final boolean centred) {
		this.centred = centred;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	/**
	 * Return's the paint used to draw the bar
	 * @return the paint used to draw the bar
	 */
	public Paint getBarPaint() {
		return barPaint;
	}

	/**
	 * Sets the paint for drawing the bar
	 * @param pBarPaint bar drawing paint
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
	 * @return the paint used to draw the text
	 */
	public Paint getTextPaint() {
		return textPaint;
	}

	/**
	 * Sets the paint for drawing the text
	 * @param pTextPaint text drawing paint
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
	 * @param pBgPaint the paint for colouring the bar background
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
	 * Sets the maximum bar length. If adjustLength is disabled this will match exactly the length of the bar.
	 * If adjustLength is enabled, the bar will be shortened to reflect a round number in length. 
	 * @param pMaxLengthInCm maximum length of the bar in the screen in cm. Default is 2.54 (=1 inch)
	 */
	public void setMaxLength(final float pMaxLengthInCm) {
		this.maxLength = pMaxLengthInCm;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void drawSafe(final ISafeCanvas c, final MapView mapView, final boolean shadow) {

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

			final IGeoPoint center = projection.fromPixels((screenWidth / 2), screenHeight / 2);
			if (zoomLevel != lastZoomLevel
					|| (int) (center.getLatitudeE6() / 1E6) != (int) (lastLatitude / 1E6)) {
				lastZoomLevel = zoomLevel;
				lastLatitude = center.getLatitudeE6();
				createScaleBarPicture(mapView);
			}

			mBounds.set(0, 0, scaleBarPicture.getWidth(), scaleBarPicture.getHeight());
			mBounds.offset((int) xOffset, (int) yOffset);
			if (centred && latitudeBar)
				mBounds.offset(-scaleBarPicture.getWidth() / 2, 0);
			if (centred && longitudeBar)
				mBounds.offset(0, -scaleBarPicture.getHeight() / 2);

			mBounds.set(mBounds);
			c.save();
			c.setMatrix(mIdentityMatrix);
			c.getWrappedCanvas().drawPicture(scaleBarPicture, mBounds);
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

	private void createScaleBarPicture(final MapView mapView) {
		// We want the scale bar to be as long as the closest round-number miles/kilometers
		// to 1-inch at the latitude at the current center of the screen.

		projection = mapView.getProjection();

		if (projection == null) {
			return;
		}

		// calculate dots per centimeter
		int xdpcm = (int) ((float) xdpi / 2.54);
		int ydpcm = (int) ((float) ydpi / 2.54);

		// get length in pixel
		int xLen = (int) (maxLength * xdpcm);
		int yLen = (int) (maxLength * ydpcm);

		// Two points, xLen apart, at scale bar screen location
		IGeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xLen / 2), yOffset);
		IGeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xLen / 2), yOffset);

		// get distance in meters between points
		final int xMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double xMetersAdjusted = this.adjustLength ? adjustScaleBarLength(xMeters) : xMeters;
		// get adjusted length in pixels
		final int xBarLengthPixels = (int) (xLen * xMetersAdjusted / xMeters);

		// Two points, yLen apart, at scale bar screen location
		p1 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) - (yLen / 2));
		p2 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) + (yLen / 2));

		// get distance in meters between points
		final int yMeters = ((GeoPoint) p1).distanceTo(p2);
		// get adjusted distance, shortened to the next lower number starting with 1, 2 or 5
		final double yMetersAdjusted = this.adjustLength ? adjustScaleBarLength(yMeters) : yMeters;
		// get adjusted length in pixels
		final int yBarLengthPixels = (int) (yLen * yMetersAdjusted / yMeters);

		final Canvas canvas = scaleBarPicture.beginRecording(xBarLengthPixels, yBarLengthPixels);

		// create text
		final String xMsg = scaleBarLengthText((int) xMetersAdjusted, imperial, nautical);
		final Rect xTextRect = new Rect();
		textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);
		final int xTextSpacing = (int) (xTextRect.height() / 5.0);

		final String yMsg = scaleBarLengthText((int) yMetersAdjusted, imperial, nautical);
		final Rect yTextRect = new Rect();
		textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);
		final int yTextSpacing = (int) (yTextRect.height() / 5.0);

		// paint background
		if (bgPaint != null) {
			canvas.drawRect(0, 0, yTextRect.height() + 2 * lineWidth + yTextSpacing,
					xTextRect.height() + 2 * lineWidth + xTextSpacing, bgPaint);
			if (latitudeBar)
				canvas.drawRect(yTextRect.height() + 2 * lineWidth + yTextSpacing, 0,
						xBarLengthPixels + lineWidth, xTextRect.height() + 2 * lineWidth
								+ xTextSpacing, bgPaint);
			if (longitudeBar)
				canvas.drawRect(0, xTextRect.height() + 2 * lineWidth + xTextSpacing,
						yTextRect.height() + 2 * lineWidth + yTextSpacing, yBarLengthPixels
								+ lineWidth, bgPaint);
		}

		// draw latitude bar
		if (latitudeBar) {
			canvas.drawRect(0, 0, xBarLengthPixels, lineWidth, barPaint);
			canvas.drawRect(xBarLengthPixels, 0, xBarLengthPixels + lineWidth, xTextRect.height()
					+ lineWidth + xTextSpacing, barPaint);

			if (!longitudeBar) {
				canvas.drawRect(0, 0, lineWidth, xTextRect.height() + lineWidth + xTextSpacing,
						barPaint);
			}

			canvas.drawText(xMsg, xBarLengthPixels / 2 - xTextRect.width() / 2, xTextRect.height()
					+ lineWidth + xTextSpacing, textPaint);
		}

		// draw longitude bar
		if (longitudeBar) {
			canvas.drawRect(0, 0, lineWidth, yBarLengthPixels, barPaint);
			canvas.drawRect(0, yBarLengthPixels, yTextRect.height() + lineWidth + yTextSpacing,
					yBarLengthPixels + lineWidth, barPaint);

			if (!latitudeBar) {
				canvas.drawRect(0, 0, yTextRect.height() + lineWidth + yTextSpacing, lineWidth,
						barPaint);
			}

			final float x = yTextRect.height() + lineWidth + yTextSpacing;
			final float y = yBarLengthPixels / 2 + yTextRect.width() / 2;

			canvas.rotate(-90, x, y);
			canvas.drawText(yMsg, x, y, textPaint);
		}

		scaleBarPicture.endRecording();
	}

	/**
	 * Returns a reduced length that starts with 1, 2 or 5 and trailing zeros. If set to nautical or imperial the 
	 * input will be transformed before and after the reduction so that the result holds in that respective unit.
	 * @param length length to round
	 * @return reduced, rounded (in m, nm or mi depending on setting) result
	 */
	private double adjustScaleBarLength(double length) {
		long pow = 0;
		boolean feet = false;
		if (this.imperial) {
			if (length >= GeoConstants.METERS_PER_STATUTE_MILE / 5)
				length = length / GeoConstants.METERS_PER_STATUTE_MILE;
			else {
				length = length * GeoConstants.FEET_PER_METER;
				feet = true;
			}
		} else if (this.nautical) {
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
		else if (this.imperial)
			length = length * GeoConstants.METERS_PER_STATUTE_MILE;
		else if (this.nautical)
			length = length * GeoConstants.METERS_PER_NAUTICAL_MILE;
		length *= Math.pow(10, pow);
		return length;
	}

	protected String scaleBarLengthText(final int meters, final boolean imperial,
			final boolean nautical) {
		if (this.imperial) {
			if (meters >= METERS_PER_STATUTE_MILE * 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_miles,
						(int) (meters / METERS_PER_STATUTE_MILE));

			} else if (meters >= METERS_PER_STATUTE_MILE / 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_miles,
						((int) (meters / (METERS_PER_STATUTE_MILE / 10.0))) / 10.0);
			} else {
				return resourceProxy.getString(ResourceProxy.string.format_distance_feet,
						(int) (meters * FEET_PER_METER));
			}
		} else if (this.nautical) {
			if (meters >= METERS_PER_NAUTICAL_MILE * 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_nautical_miles,
						((int) (meters / METERS_PER_NAUTICAL_MILE)));
			} else if (meters >= METERS_PER_NAUTICAL_MILE / 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_nautical_miles,
						(((int) (meters / (METERS_PER_NAUTICAL_MILE / 10.0))) / 10.0));
			} else {
				return resourceProxy.getString(ResourceProxy.string.format_distance_feet,
						((int) (meters * FEET_PER_METER)));
			}
		} else {
			if (meters >= 1000 * 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_kilometers,
						(meters / 1000));
			} else if (meters >= 1000 / 5) {
				return resourceProxy.getString(ResourceProxy.string.format_distance_kilometers,
						(int) (meters / 100.0) / 10.0);
			} else {
				return resourceProxy.getString(ResourceProxy.string.format_distance_meters, meters);
			}
		}
	}

}

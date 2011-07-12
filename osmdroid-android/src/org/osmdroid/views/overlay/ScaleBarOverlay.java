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

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Picture;
import android.graphics.Rect;

public class ScaleBarOverlay extends Overlay implements GeoConstants {

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

	private final Activity activity;

	protected final Picture scaleBarPicture = new Picture();

	private int lastZoomLevel = -1;
	private float lastLatitude = 0;

	public float xdpi;
	public float ydpi;
	public int screenWidth;
	public int screenHeight;

	private final ResourceProxy resourceProxy;
	private Paint barPaint;
	private Paint textPaint;
	private Projection projection;

	final private Rect mBounds = new Rect();

	// ===========================================================
	// Constructors
	// ===========================================================

	public ScaleBarOverlay(final Activity activity) {
		this(activity, new DefaultResourceProxyImpl(activity));
	}

	public ScaleBarOverlay(final Activity activity, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		this.resourceProxy = pResourceProxy;
		this.activity = activity;

		this.barPaint = new Paint();
		this.barPaint.setColor(Color.BLACK);
		this.barPaint.setAntiAlias(true);
		this.barPaint.setStyle(Style.FILL);
		this.barPaint.setAlpha(255);

		this.textPaint = new Paint();
		this.textPaint.setColor(Color.BLACK);
		this.textPaint.setAntiAlias(true);
		this.textPaint.setStyle(Style.FILL);
		this.textPaint.setAlpha(255);
		this.textPaint.setTextSize(textSize);

		this.xdpi = this.activity.getResources().getDisplayMetrics().xdpi;
		this.ydpi = this.activity.getResources().getDisplayMetrics().ydpi;

		this.screenWidth = this.activity.getResources().getDisplayMetrics().widthPixels;
		this.screenHeight = this.activity.getResources().getDisplayMetrics().heightPixels;

		// DPI corrections for specific models
		String manufacturer = null;
		try {
			final Field field = android.os.Build.class.getField("MANUFACTURER");
			manufacturer = (String) field.get(null);
		} catch (final Exception ignore) {
		}

		if ("motorola".equals(manufacturer) && "DROIDX".equals(android.os.Build.MODEL)) {

			// If the screen is rotated, flip the x and y dpi values
			if (activity.getWindowManager().getDefaultDisplay().getOrientation() > 0) {
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

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setMinZoom(final int zoom) {
		this.minZoom = zoom;
	}

	public void setScaleBarOffset(final float x, final float y) {
		xOffset = x;
		yOffset = y;
	}

	public void setLineWidth(final float width) {
		this.lineWidth = width;
	}

	public void setTextSize(final float size) {
		this.textPaint.setTextSize(size);
	}

	public void setImperial() {
		this.imperial = true;
		this.nautical = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	public void setNautical() {
		this.nautical = true;
		this.imperial = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	public void setMetric() {
		this.nautical = false;
		this.imperial = false;
		lastZoomLevel = -1; // Force redraw of scalebar
	}

	public void drawLatitudeScale(final boolean latitude) {
		this.latitudeBar = latitude;
	}

	public void drawLongitudeScale(final boolean longitude) {
		this.longitudeBar = longitude;
	}

	public Paint getBarPaint() {
		return barPaint;
	}

	public void setBarPaint(final Paint pBarPaint) {
		if (pBarPaint == null) {
			throw new IllegalArgumentException("pBarPaint argument cannot be null");
		}
		barPaint = pBarPaint;
	}

	public Paint getTextPaint() {
		return textPaint;
	}

	public void setTextPaint(final Paint pTextPaint) {
		if (pTextPaint == null) {
			throw new IllegalArgumentException("pTextPaint argument cannot be null");
		}
		textPaint = pTextPaint;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void draw(final Canvas c, final MapView mapView, final boolean shadow) {

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

			mBounds.set(projection.getScreenRect());
			mBounds.offset((int) xOffset, (int) yOffset);

			mBounds.set(mBounds.left, mBounds.top, mBounds.left + scaleBarPicture.getWidth(),
					mBounds.top + scaleBarPicture.getHeight());
			c.drawPicture(scaleBarPicture, mBounds);
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

		// Two points, 1-inch apart in x/latitude, centered on screen
		GeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xdpi / 2), screenHeight / 2);
		GeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xdpi / 2), screenHeight / 2);

		final int xMetersPerInch = p1.distanceTo(p2);

		p1 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) - (ydpi / 2));
		p2 = projection.fromPixels(screenWidth / 2, (screenHeight / 2) + (ydpi / 2));

		final int yMetersPerInch = p1.distanceTo(p2);

		final Canvas canvas = scaleBarPicture.beginRecording((int) xdpi, (int) ydpi);

		if (latitudeBar) {
			final String xMsg = scaleBarLengthText(xMetersPerInch, imperial, nautical);
			final Rect xTextRect = new Rect();
			textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);

			final int textSpacing = (int) (xTextRect.height() / 5.0);

			canvas.drawRect(0, 0, xdpi, lineWidth, barPaint);
			canvas.drawRect(xdpi, 0, xdpi + lineWidth,
					xTextRect.height() + lineWidth + textSpacing, barPaint);

			if (!longitudeBar) {
				canvas.drawRect(0, 0, lineWidth, xTextRect.height() + lineWidth + textSpacing,
						barPaint);
			}

			canvas.drawText(xMsg, xdpi / 2 - xTextRect.width() / 2, xTextRect.height() + lineWidth
					+ textSpacing, textPaint);
		}

		if (longitudeBar) {
			final String yMsg = scaleBarLengthText(yMetersPerInch, imperial, nautical);
			final Rect yTextRect = new Rect();
			textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);

			final int textSpacing = (int) (yTextRect.height() / 5.0);

			canvas.drawRect(0, 0, lineWidth, ydpi, barPaint);
			canvas.drawRect(0, ydpi, yTextRect.height() + lineWidth + textSpacing,
					ydpi + lineWidth, barPaint);

			if (!latitudeBar) {
				canvas.drawRect(0, 0, yTextRect.height() + lineWidth + textSpacing, lineWidth,
						barPaint);
			}

			final float x = yTextRect.height() + lineWidth + textSpacing;
			final float y = ydpi / 2 + yTextRect.width() / 2;

			canvas.rotate(-90, x, y);
			canvas.drawText(yMsg, x, y + textSpacing, textPaint);

		}

		scaleBarPicture.endRecording();
	}

	private String scaleBarLengthText(final int meters, final boolean imperial,
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

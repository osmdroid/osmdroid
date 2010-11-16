package org.andnav.osm.views.overlay;

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
 * OpenStreetMapView map = new OpenStreetMapView(...);
 * ScaleBarOverlay scaleBar = new ScaleBarOverlay(this.getBaseContext(), map);
 * 
 * scaleBar.setImperial(); // Metric by default
 * 
 * 
 * map.getOverlays().add(scaleBar);
 * 
 * To Do List:
 * 1. Allow for top, bottom, left or right placement.
 * 2. Scale bar to precise displayed scale text after rounding.
 * 
 */

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.constants.GeoConstants;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class ScaleBarOverlay extends OpenStreetMapViewOverlay implements GeoConstants {

	// ===========================================================
	// Fields
	// ===========================================================

	// Defaults
	
	boolean enabled = true;

	float xOffset = 10;
	float yOffset = 10;
	float lineWidth = 2;
	int textSize = 12;
	int minZoom = 0;

	boolean imperial = false;
	boolean nautical = false;
		
	boolean latitudeBar = true;
	boolean longitudeBar = false;
		
	// Internal
	
	private Activity activity;
	
	protected final Picture scaleBarPicture = new Picture();
	
	private int lastZoomLevel = -1;
	private float lastLatitude = 0;

	public float xdpi;
	public float ydpi;
	public int screenWidth;
	public int screenHeight;
	
	private ResourceProxy resourceProxy;
	private Matrix oldMatrix;
	private Paint barPaint;
	private Paint textPaint;
	private OpenStreetMapViewProjection projection;
	
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
		if (android.os.Build.MANUFACTURER.equals("motorola") && android.os.Build.MODEL.equals("DROIDX")) {

			// If the screen is rotated, flip the x and y dpi values
			if (activity.getWindowManager().getDefaultDisplay().getOrientation() > 0) {
				this.xdpi = (float)(this.screenWidth/3.75);
				this.ydpi = (float)(this.screenHeight/2.1);				
			} else {
				this.xdpi = (float)(this.screenWidth/2.1);
				this.ydpi = (float)(this.screenHeight/3.75);
			}
			
		} else if (android.os.Build.MANUFACTURER.equals("motorola") && android.os.Build.MODEL.equals("Droid")) {
			// http://www.mail-archive.com/android-developers@googlegroups.com/msg109497.html
			this.xdpi = 264;
			this.ydpi = 264;
		}
				
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setMinZoom(int zoom) {
		this.minZoom = zoom;
	}
	
	public void setScaleBarOffset(float x, float y) {
		xOffset = x;
		yOffset = y;
	}
	
	public void setLineWidth(float width) {
		this.lineWidth = width;
	}
	
	public void setTextSize(int size) {
		this.textSize = size;
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
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void drawLatitudeScale(boolean latitude) {
		this.latitudeBar = latitude;
	}
	
	public void drawLongitudeScale(boolean longitude) {
		this.longitudeBar = longitude;
	}
	
	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {}

	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView mapView) {

		// If map view is animating, don't update, scale will be wrong.
		if (mapView.isAnimating())
			return;
		
		final int zoomLevel = mapView.getZoomLevel();
		
		if (this.enabled && zoomLevel >= minZoom) {
			OpenStreetMapViewProjection projection = mapView.getProjection();
			
			if (projection == null) {
				return;
			}

			GeoPoint center = projection.fromPixels((screenWidth / 2), screenHeight/2);
			if (zoomLevel != lastZoomLevel || (int)(center.getLatitudeE6()/1E6) != (int)(lastLatitude/1E6)) {
				lastZoomLevel = zoomLevel;
				lastLatitude = center.getLatitudeE6();
				createScaleBarPicture(mapView);
			}
			
			this.oldMatrix = c.getMatrix();
			c.restore();
			c.save();
			c.translate(xOffset, yOffset);
			c.drawPicture(scaleBarPicture);
			c.setMatrix(this.oldMatrix);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void disableScaleBar() {
		this.enabled = false;
	}

	public boolean enableScaleBar() {
		return this.enabled = true;
	}
	
	private void createScaleBarPicture(final OpenStreetMapView mapView) {
		// We want the scale bar to be as long as the closest round-number miles/kilometers
		// to 1-inch at the latitude at the current center of the screen.
				
		projection = mapView.getProjection();
	
		if (projection == null) {
			return;
		}
		
		// Two points, 1-inch apart in x/latitude, centered on screen
		GeoPoint p1 = projection.fromPixels((screenWidth / 2) - (xdpi / 2), screenHeight/2);
		GeoPoint p2 = projection.fromPixels((screenWidth / 2) + (xdpi / 2), screenHeight/2);

		int xMetersPerInch = p1.distanceTo(p2);

		p1 = projection.fromPixels(screenWidth/2, (screenHeight / 2) - (ydpi / 2));
		p2 = projection.fromPixels(screenWidth/2, (screenHeight / 2) + (ydpi / 2));

		int yMetersPerInch = p1.distanceTo(p2);

		final Canvas canvas = scaleBarPicture.beginRecording((int)xdpi, (int)ydpi);
				
		if (latitudeBar) {
			String xMsg = scaleBarLengthText(xMetersPerInch, imperial, nautical);
			Rect xTextRect = new Rect();
			textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);

			int textSpacing = (int)(xTextRect.height() / 5.0);
			
			canvas.drawRect(0,0 , xdpi, lineWidth, barPaint);
			canvas.drawRect(xdpi, 0, xdpi + lineWidth, xTextRect.height() + lineWidth + textSpacing, barPaint);
			
			if (! longitudeBar) {
				canvas.drawRect(0, 0, lineWidth, xTextRect.height() + lineWidth + textSpacing, barPaint);
			}

			canvas.drawText(xMsg, xdpi/2 - xTextRect.width()/2, xTextRect.height() + lineWidth + textSpacing, textPaint);
		}
		
		if (longitudeBar) {
			String yMsg = scaleBarLengthText(yMetersPerInch, imperial, nautical);
			Rect yTextRect = new Rect();
			textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);

			int textSpacing = (int)(yTextRect.height() / 5.0);

			canvas.drawRect(0, 0, lineWidth, ydpi, barPaint);
			canvas.drawRect(0, ydpi, yTextRect.height() + lineWidth + textSpacing, ydpi + lineWidth, barPaint);

			if (! latitudeBar) {
				canvas.drawRect(0, 0, yTextRect.height() + lineWidth + textSpacing, lineWidth, barPaint);
			}			
			
			float x = yTextRect.height() + lineWidth + textSpacing;
			float y = ydpi/2 + yTextRect.width()/2;

			canvas.rotate(-90, x, y);
			canvas.drawText(yMsg, x, y + textSpacing, textPaint);

		}

		scaleBarPicture.endRecording();
	}
	
	private String scaleBarLengthText(int meters, boolean imperial, boolean nautical) {
		if (this.imperial) {
			if (meters >= METERS_PER_STATUTE_MILE) {
				return 	resourceProxy.getString(
						ResourceProxy.string.format_distance_miles,
						(int)(meters / METERS_PER_STATUTE_MILE));
				
			} else if (meters >= METERS_PER_STATUTE_MILE/10) {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_miles,
						((int)(meters / (METERS_PER_STATUTE_MILE / 10.0))) / 10.0);
			} else {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_feet,
						(int)(meters * FEET_PER_METER));
			}
		} else if (this.nautical) {
			if (meters >= METERS_PER_NAUTICAL_MILE) {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_nautical_miles,
						((int)(meters / METERS_PER_NAUTICAL_MILE)));
			} else if (meters >= METERS_PER_NAUTICAL_MILE / 10.0) {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_nautical_miles,
						(((int)(meters / (METERS_PER_NAUTICAL_MILE / 10.0))) / 10.0));
			} else {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_feet,
						((int)(meters * FEET_PER_METER)));
			}
		} else {
			if (meters >= 1000) {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_kilometers,
						(int)(meters/1000));
			} else if (meters > 100) {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_kilometers,
						(int)(meters / 100.0) / 10.0);
			} else {
				return resourceProxy.getString(
						ResourceProxy.string.format_distance_meters,
						(int)meters);
			}
		}
	}

}

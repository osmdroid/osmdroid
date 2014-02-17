package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.util.constants.MathConstants;

import android.content.Context;
import android.graphics.Color;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points. 
 *  Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support Z-Index: drawing order is the order in map overlays<br/>
 * 
 * Implementation: inherits from PathOverlay, then adds Google API compatibility and Geodesic mode. 
 * 
 * @author M.Kergall
 */
public class Polyline extends PathOverlay {
	
	/** original GeoPoints */
	private int mOriginalPoints[][]; //as an array, to reduce object creation
	protected boolean mGeodesic;
	
	public Polyline(Context ctx){
		this(new DefaultResourceProxyImpl(ctx));
	}
	
	public Polyline(final ResourceProxy resourceProxy){
		//default as defined in Google API:
		super(Color.BLACK, 10.0f, resourceProxy);
		mPaint.setAntiAlias(true);
		mOriginalPoints = new int[0][2];
		mGeodesic = false;
	}
	
	public List<GeoPoint> getPoints(){
		List<GeoPoint> result = new ArrayList<GeoPoint>(mOriginalPoints.length);
		for (int i=0; i<mOriginalPoints.length; i++){
			GeoPoint gp = new GeoPoint(mOriginalPoints[i][0], mOriginalPoints[i][1]);
			result.add(gp);
		}
		return result;
	}
	
	public int getColor(){
		return mPaint.getColor();
	}
	
	public float getWidth(){
		return mPaint.getStrokeWidth();
	}
	
	public boolean isVisible(){
		return isEnabled();
	}
	
	public boolean isGeodesic(){
		return mGeodesic;
	}
	
	public void setColor(int color){
		mPaint.setColor(color);
	}
	
	public void setWidth(float width){
		mPaint.setStrokeWidth(width);
	}
	
	public void setVisible(boolean visible){
		setEnabled(visible);
	}
	
	public void addGreatCircle(final GeoPoint startPoint, final GeoPoint endPoint, final int numberOfPoints) {
		//	adapted from page http://compastic.blogspot.co.uk/2011/07/how-to-draw-great-circle-on-map-in.html
		//	which was adapted from page http://maps.forum.nu/gm_flight_path.html

		// convert to radians
		final double lat1 = startPoint.getLatitude() * MathConstants.DEG2RAD;
		final double lon1 = startPoint.getLongitude() * MathConstants.DEG2RAD;
		final double lat2 = endPoint.getLatitude() * MathConstants.DEG2RAD;
		final double lon2 = endPoint.getLongitude() * MathConstants.DEG2RAD;

		final double d = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2)
				* Math.pow(Math.sin((lon1 - lon2) / 2), 2)));
		double bearing = Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2),
				Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2))
				/ -MathConstants.DEG2RAD;
		bearing = bearing < 0 ? 360 + bearing : bearing;
		
		for (int i = 1; i <= numberOfPoints; i++) {
			final double f = 1.0 * i / (numberOfPoints+1);
			final double A = Math.sin((1 - f) * d) / Math.sin(d);
			final double B = Math.sin(f * d) / Math.sin(d);
			final double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
			final double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
			final double z = A * Math.sin(lat1) + B * Math.sin(lat2);

			final double latN = Math.atan2(z, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
			final double lonN = Math.atan2(y, x);
			addPoint((int) (latN * MathConstants.RAD2DEG * 1E6), (int) (lonN * MathConstants.RAD2DEG * 1E6));
		}
	}
	
	public void setPoints(List<GeoPoint> points){
		clearPath();
		int size = points.size();
		mOriginalPoints = new int[size][2];
		for (int i=0; i<size; i++){
			GeoPoint p = points.get(i);
			mOriginalPoints[i][0] = p.getLatitudeE6();
			mOriginalPoints[i][1] = p.getLongitudeE6();
			if (!mGeodesic){
				super.addPoint(p);
			} else {
				if (i>0){
					//add potential intermediate points:
					GeoPoint prev = points.get(i-1);
					final int greatCircleLength = prev.distanceTo(p);
					//add one point for every 100kms of the great circle path
					final int numberOfPoints = greatCircleLength/100000;
					addGreatCircle(prev, p, numberOfPoints);
				}
				super.addPoint(p);
			}
		}
	}
	
	public void setGeodesic(boolean geodesic){
		mGeodesic = geodesic;
	}
	
}

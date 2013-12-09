package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;
import android.content.Context;
import android.graphics.Color;

/**
 * A polyline is a list of points, where line segments are drawn between consecutive points. 
 *  Mimics the Polyline class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support: Z-Index, Geodesic mode<br/>
 * 
 * Implementation: inherits from PathOverlay, and adds Google API compatibility. 
 * 
 * @author M.Kergall
 */
public class Polyline extends PathOverlay {
	
	/** original GeoPoints */
	private List<GeoPoint> mOriginalPoints;

	public Polyline(Context ctx){
		this(new DefaultResourceProxyImpl(ctx));
	}
	
	public Polyline(final ResourceProxy resourceProxy){
		//default as defined in Google API:
		super(Color.BLACK, 10.0f, resourceProxy);
		mOriginalPoints = new ArrayList<GeoPoint>();
	}
	
	public List<GeoPoint> getPoints(){
		List<GeoPoint> result = new ArrayList<GeoPoint>(mOriginalPoints.size());
		for (GeoPoint p:mOriginalPoints){
			GeoPoint gp = (GeoPoint) p.clone();
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
	
	public void setColor(int color){
		mPaint.setColor(color);
	}
	
	public void setWidth(float width){
		mPaint.setStrokeWidth(width);
	}
	
	public void setVisible(boolean visible){
		setEnabled(visible);
	}
	
	public void setPoints(List<GeoPoint> points){
		for (GeoPoint p:points)
			addPoint(p.getLatitudeE6(), p.getLongitudeE6());
	}
	
	public void addPoint(final int latitudeE6, final int longitudeE6){
		mOriginalPoints.add(new GeoPoint(latitudeE6, longitudeE6));
		super.addPoint(latitudeE6, longitudeE6);
	}
	
}

package org.osmdroid.bonuspack.overlays;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * A ground overlay is an image that is fixed to a map. 
 * Mimics the GroundOverlay class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support: Z-Index, setPositionFromBounds<br/>
 * - image can be any standard Android Drawable, instead of the BitmapDescriptor introduced in Maps API. <br/>
 * 
 * @author M.Kergall
 *
 */
public class GroundOverlay extends SafeDrawOverlay {

	protected Drawable mImage;
	protected GeoPoint mPosition;
	protected float mBearing;
	protected float mWidth, mHeight;
	protected float mTransparency;
	public final static float NO_DIMENSION = -1.0f;
	protected Point mPositionPixels, mSouthEastPixels;
	
	public GroundOverlay(Context ctx) {
		this(new DefaultResourceProxyImpl(ctx));
	}

	public GroundOverlay(final ResourceProxy resourceProxy) {
		super(resourceProxy);
		mWidth = 10.0f;
		mHeight = NO_DIMENSION;
		mBearing = 0.0f;
		mTransparency = 0.0f;
		mPositionPixels = new Point();
		mSouthEastPixels = new Point();
	}

	public void setImage(Drawable image){
		mImage = image;
	}
	
	public Drawable getImage(){
		return mImage;
	}
	
	public GeoPoint getPosition(){
		return mPosition.clone();
	}
	
	public void setPosition(GeoPoint position){
		mPosition = position.clone();
	}

	public float getBearing(){
		return mBearing;
	}
	
	public void setBearing(float bearing){
		mBearing = bearing;
	}
	
	public void setDimensions(float width){
		mWidth = width;
		mHeight = NO_DIMENSION;
	}
	
	public void setDimensions(float width, float height){
		mWidth = width;
		mHeight = height;
	}
	
	public float getHeight(){
		return mHeight;
	}
	
	public float getWidth(){
		return mWidth;
	}
	
	public void setTransparency(float transparency){
		mTransparency = transparency;
	}
	
	public float getTransparency(){
		return mTransparency;
	}
	
	@Override protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		if (shadow)
			return;
		if (mImage == null)
			return;
		
		if (mHeight == NO_DIMENSION){
			mHeight = mWidth * mImage.getIntrinsicHeight() / mImage.getIntrinsicWidth();
		}
		
		final Projection pj = mapView.getProjection();
		
		pj.toMapPixels(mPosition, mPositionPixels);
		GeoPoint pEast = mPosition.destinationPoint(mWidth, 90.0f);
		GeoPoint pSouthEast = pEast.destinationPoint(mHeight, -180.0f);
		pj.toMapPixels(pSouthEast, mSouthEastPixels);
		int width = mSouthEastPixels.x-mPositionPixels.x;
		int height = mSouthEastPixels.y-mPositionPixels.y;
		mImage.setBounds(-width/2, -height/2, width/2, height/2);
		
		mImage.setAlpha(255-(int)(mTransparency*255));

		drawAt(canvas.getSafeCanvas(), mImage, mPositionPixels.x, mPositionPixels.y, false, -mBearing);
	}

}

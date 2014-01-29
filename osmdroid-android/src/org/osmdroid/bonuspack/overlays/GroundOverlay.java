package org.osmdroid.bonuspack.overlays;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GroundOverlay extends SafeDrawOverlay {

	protected Drawable mImage;
	protected GeoPoint mPosition;
	protected float mBearing;
	protected float mWidth, mHeight;
	protected float mTransparency;
	protected final static float UNDEFINED_DIM = -1.0f;
	protected Point mPositionPixels;
	
	public GroundOverlay(Context ctx) {
		this(new DefaultResourceProxyImpl(ctx));
	}

	public GroundOverlay(final ResourceProxy resourceProxy) {
		super(resourceProxy);
		mWidth = 10.0f;
		mHeight = UNDEFINED_DIM;
		mBearing = 0.0f;
		mTransparency = 0.0f;
		mPositionPixels = new Point();
	}

	public void setImage(Drawable image){
		mImage = image;
	}
	
	public void setPosition(GeoPoint position){
		mPosition = position.clone();
	}
	
	public void setBearing(float bearing){
		mBearing = bearing;
	}
	
	public void setDimensions(float width){
		mWidth = width;
		mHeight = UNDEFINED_DIM;
	}
	
	public void setDimensions(float width, float height){
		mWidth = width;
		mHeight = height;
	}
	
	public void setTransparency(float transparency){
		mTransparency = transparency;
	}
	
	@Override protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		if (shadow)
			return;
		if (mImage == null)
			return;
		
		if (mHeight == UNDEFINED_DIM){
			mHeight = mWidth * mImage.getIntrinsicHeight() / mImage.getIntrinsicWidth();
		}
		
		final Projection pj = mapView.getProjection();
		
		pj.toMapPixels(mPosition, mPositionPixels);
		GeoPoint p2 = mPosition.destinationPoint(mWidth, 90.0f);
		GeoPoint p3 = p2.destinationPoint(mHeight, -180.0f);
		Point endPixels = pj.toMapPixels(p3, null);
		mImage.setBounds(0, 0, endPixels.x-mPositionPixels.x, endPixels.y-mPositionPixels.y);
		
		/*
		Matrix matrix = new Matrix();
		matrix.setTranslate(mPositionPixels.x, mPositionPixels.y);
		matrix.setRotate(mBearing);
		//matrix.setScale(pj.metersToEquatorPixels(mWidth), pj.metersToEquatorPixels(mHeight));
		 */
		drawAt(canvas.getSafeCanvas(), mImage, mPositionPixels.x, mPositionPixels.y, false, 0.0f);
	}

}

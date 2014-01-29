package org.osmdroid.bonuspack.overlays;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GroundOverlay extends Overlay {

	protected Bitmap mImage;
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

	public void setImage(Bitmap image){
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
	
	@Override protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow)
			return;
		if (mImage == null)
			return;
		
		if (mHeight == UNDEFINED_DIM){
			mHeight = mWidth * mImage.getHeight() / mImage.getWidth();
		}
		
		final Projection pj = mapView.getProjection();
		Point tmp = pj.toMapPixelsProjected(mPosition.getLatitudeE6(), mPosition.getLongitudeE6(), null);
		pj.toMapPixelsTranslated(tmp, mPositionPixels);
		Matrix matrix = new Matrix();
		matrix.setTranslate(mPositionPixels.x, mPositionPixels.y);
		matrix.setRotate(mBearing);
		matrix.setScale(pj.metersToEquatorPixels(mWidth), pj.metersToEquatorPixels(mHeight));
		canvas.drawBitmap(mImage, matrix, null);
	}

}

package org.osmdroid.views.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * A ground overlay is an image that is fixed to 2 corners on a map using simple scaling
 * that does not take into consideration the curvature of the Earth.
 * @since 6.0.0
 * @author pasniak inspired by zkhan's code in Avare
 *
 */
@SuppressWarnings("WeakerAccess")
public class GroundOverlay2 extends Overlay {

    private float   mLonL,
                    mLatU,
                    mLonR,
                    mLatD;
    protected Bitmap mImage;
	protected float mBearing;
	protected float mTransparency;
    private Matrix mStretchToFitTransformationMatrix;

	public GroundOverlay2() {
		super();
		mBearing = 0.0f;
		mTransparency = 0.0f;
        mStretchToFitTransformationMatrix = new Matrix();
	}

	public void setImage(Bitmap image){
		mImage = image;
	}

	public Bitmap getImage(){
		return mImage;
	}

	public void setPosition(GeoPoint UL, GeoPoint RD)
    {
        mLatU = (float)UL.getLatitude();
        mLonL = (float)UL.getLongitude();
        mLatD = (float)RD.getLatitude();
        mLonR = (float)RD.getLongitude();
	}

	public float getBearing(){
		return mBearing;
	}
	
	public void setBearing(float bearing){
		mBearing = bearing;
	}
	
	public void setTransparency(float transparency){
		mTransparency = transparency;
	}
	
	public float getTransparency(){
		return mTransparency;
	}
	

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
		if(null == mImage || shadow) {
			return;
		}

        final Projection pj = mapView.getProjection();

        long x0 = pj.getLongPixelXFromLongitude(mLonL),
             y0 = pj.getLongPixelYFromLatitude(mLatU),
             x1 = pj.getLongPixelXFromLongitude(mLonR),
             y1 = pj.getLongPixelYFromLatitude(mLatD);

        float widthOnTheMap = x1 - x0,
              heightOnTheMap = y1 - y0;

		float scaleX = widthOnTheMap / mImage.getWidth(),
		      scaleY = heightOnTheMap / mImage.getHeight();

        setupScalingThenTranslatingMatrix(scaleX, scaleY, x0, y0);

        Paint paint = new Paint();
        paint.setAlpha(255-(int)(mTransparency * 255));
		canvas.drawBitmap(mImage, mStretchToFitTransformationMatrix, paint);
	}

    private void setupScalingThenTranslatingMatrix(float scaleX, float scaleY, long x0, long y0) {
        mStretchToFitTransformationMatrix.setScale(scaleX, scaleY);
        mStretchToFitTransformationMatrix.postTranslate(x0, y0);
    }
}

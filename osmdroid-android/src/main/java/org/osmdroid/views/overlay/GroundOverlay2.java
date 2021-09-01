package org.osmdroid.views.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

/**
 * A ground overlay is an image that is fixed to 2 corners on a map using simple scaling
 * that does not take into consideration the curvature of the Earth.
 *
 * @author pasniak inspired by zkhan's code in Avare
 * @since 6.0.0
 * @deprecated Use {@link GroundOverlay} instead
 */
@Deprecated
public class GroundOverlay2 extends Overlay {

    private final Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();

    protected float mBearing;
    protected float mTransparency;
    private Bitmap mImage;

    public GroundOverlay2() {
        super();
        mBearing = 0.0f;
        setTransparency(0.0f);
    }

    protected Paint getPaint() {
        return mPaint;
    }

    protected Matrix getMatrix() {
        return mMatrix;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearing(final float pBearing) {
        mBearing = pBearing;
    }

    public void setTransparency(final float pTransparency) {
        mTransparency = pTransparency;
        mPaint.setAlpha(255 - (int) (mTransparency * 255));
    }

    public float getTransparency() {
        return mTransparency;
    }

    public void setImage(final Bitmap pImage) {
        mImage = pImage;
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        if (mImage == null) {
            return;
        }
        computeMatrix(pProjection);
        pCanvas.drawBitmap(getImage(), getMatrix(), getPaint());
    }

    private float mLonL,
            mLatU,
            mLonR,
            mLatD;

    /**
     * @param UL upper left
     * @param RD lower right
     */
    public void setPosition(GeoPoint UL, GeoPoint RD) {
        mLatU = (float) UL.getLatitude();
        mLonL = (float) UL.getLongitude();
        mLatD = (float) RD.getLatitude();
        mLonR = (float) RD.getLongitude();
    }

    protected void computeMatrix(final Projection pProjection) {
        long x0 = pProjection.getLongPixelXFromLongitude(mLonL),
                y0 = pProjection.getLongPixelYFromLatitude(mLatU),
                x1 = pProjection.getLongPixelXFromLongitude(mLonR),
                y1 = pProjection.getLongPixelYFromLatitude(mLatD);

        float widthOnTheMap = x1 - x0,
                heightOnTheMap = y1 - y0;

        float scaleX = widthOnTheMap / getImage().getWidth(),
                scaleY = heightOnTheMap / getImage().getHeight();

        getMatrix().setScale(scaleX, scaleY);
        getMatrix().postTranslate(x0, y0);
    }
}

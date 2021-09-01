package org.osmdroid.views.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

/**
 * Place an image on the map, each corner of the image being associated with a {@link GeoPoint}
 *
 * @author Fabrice Fontaine
 * Triggered by issue 1361 (https://github.com/osmdroid/osmdroid/issues/1361)
 * Inspired by {@link GroundOverlay2}
 * @since 6.1.1
 * @deprecated Use {@link GroundOverlay} instead
 */
@Deprecated
public class GroundOverlay4 extends Overlay {

    private final Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();

    protected float mBearing;
    protected float mTransparency;
    private Bitmap mImage;

    public GroundOverlay4() {
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

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        if (mImage == null) {
            return;
        }
        computeMatrix(pProjection);
        pCanvas.drawBitmap(getImage(), getMatrix(), getPaint());
    }

    private final float[] mMatrixSrc = new float[8];
    private final float[] mMatrixDst = new float[8];

    private GeoPoint mTopLeft;
    private GeoPoint mTopRight;
    private GeoPoint mBottomRight;
    private GeoPoint mBottomLeft;

    public void setImage(final Bitmap pImage) {
        mImage = pImage;
        if (getImage() == null) {
            return;
        }
        final int width = getImage().getWidth();
        final int height = getImage().getHeight();
        mMatrixSrc[0] = 0;
        mMatrixSrc[1] = 0;
        mMatrixSrc[2] = width;
        mMatrixSrc[3] = 0;
        mMatrixSrc[4] = width;
        mMatrixSrc[5] = height;
        mMatrixSrc[6] = 0;
        mMatrixSrc[7] = height;
    }

    public void setPosition(final GeoPoint pTopLeft, final GeoPoint pTopRight,
                            final GeoPoint pBottomRight, final GeoPoint pBottomLeft) {
        mTopLeft = new GeoPoint(pTopLeft);
        mTopRight = new GeoPoint(pTopRight);
        mBottomRight = new GeoPoint(pBottomRight);
        mBottomLeft = new GeoPoint(pBottomLeft);
    }

    protected void computeMatrix(final Projection pProjection) {
        final long topLeftCornerX = pProjection.getLongPixelXFromLongitude(mTopLeft.getLongitude());
        final long topLeftCornerY = pProjection.getLongPixelYFromLatitude(mTopLeft.getLatitude());
        final long topRightCornerX = pProjection.getLongPixelXFromLongitude(mTopRight.getLongitude());
        final long topRightCornerY = pProjection.getLongPixelYFromLatitude(mTopRight.getLatitude());
        final long bottomRightCornerX = pProjection.getLongPixelXFromLongitude(mBottomRight.getLongitude());
        final long bottomRightCornerY = pProjection.getLongPixelYFromLatitude(mBottomRight.getLatitude());
        final long bottomLeftCornerX = pProjection.getLongPixelXFromLongitude(mBottomLeft.getLongitude());
        final long bottomLeftCornerY = pProjection.getLongPixelYFromLatitude(mBottomLeft.getLatitude());

        mMatrixDst[0] = (float) topLeftCornerX;
        mMatrixDst[1] = (float) topLeftCornerY;
        mMatrixDst[2] = (float) topRightCornerX;
        mMatrixDst[3] = (float) topRightCornerY;
        mMatrixDst[4] = (float) bottomRightCornerX;
        mMatrixDst[5] = (float) bottomRightCornerY;
        mMatrixDst[6] = (float) bottomLeftCornerX;
        mMatrixDst[7] = (float) bottomLeftCornerY;

        getMatrix().setPolyToPoly(mMatrixSrc, 0, mMatrixDst, 0, 4);
    }
}

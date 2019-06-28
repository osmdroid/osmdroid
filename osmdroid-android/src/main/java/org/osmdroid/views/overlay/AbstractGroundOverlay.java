package org.osmdroid.views.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import org.osmdroid.views.Projection;

/**
 * @since 6.1.1
 * @author Fabrice Fontaine
 * Triggered by the convergence between {@link GroundOverlay2} and {@link GroundOverlay4}
 */
public abstract class AbstractGroundOverlay extends Overlay {

    private final Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();

    protected float mBearing;
    protected float mTransparency;
    private Bitmap mImage;

    public AbstractGroundOverlay() {
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

    public float getBearing(){
        return mBearing;
    }

    public void setBearing(final float pBearing){
        mBearing = pBearing;
    }

    public void setTransparency(final float pTransparency){
        mTransparency = pTransparency;
        mPaint.setAlpha(255-(int)(mTransparency * 255));
    }

    public float getTransparency(){
        return mTransparency;
    }

    public void setImage(final Bitmap pImage){
        mImage = pImage;
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        if(getImage() == null) {
            return;
        }
        computeMatrix(pProjection);
        pCanvas.drawBitmap(getImage(), getMatrix(), getPaint());
    }

    abstract protected void computeMatrix(final Projection pProjection);
}

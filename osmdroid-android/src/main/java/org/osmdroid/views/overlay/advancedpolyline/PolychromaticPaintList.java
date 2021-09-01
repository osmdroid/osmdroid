package org.osmdroid.views.overlay.advancedpolyline;

import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import org.osmdroid.views.overlay.PaintList;

/**
 * A real {@link PaintList} with potentially different colors for each segment, and linear gradients
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */
public class PolychromaticPaintList implements PaintList {

    private final Paint mPaint;
    private final ColorMapping mColorMapping;
    private final boolean mUseGradient;

    /**
     * @param pPaint        Basis Paint
     * @param pColorMapping from where we get the color to use for each geo segment
     * @param pUseGradient  should we use a gradient from this segment's color to the next segment's
     */
    public PolychromaticPaintList(final Paint pPaint, final ColorMapping pColorMapping, final boolean pUseGradient) {
        mPaint = pPaint;
        mColorMapping = pColorMapping;
        mUseGradient = pUseGradient;
    }

    @Override
    public Paint getPaint() {
        return null;
    }

    @Override
    public Paint getPaint(final int pIndex, final float pX0, final float pY0, final float pX1, final float pY1) {
        final int startColor = mColorMapping.getColorForIndex(pIndex);
        if (mUseGradient) {
            final int endColor = mColorMapping.getColorForIndex(pIndex + 1);
            if (startColor != endColor) {
                final Shader shader = new LinearGradient(pX0, pY0, pX1, pY1, startColor, endColor, Shader.TileMode.CLAMP);
                mPaint.setShader(shader);
                return mPaint;
            }
            mPaint.setShader(null);
        }
        mPaint.setColor(startColor);
        return mPaint;
    }
}
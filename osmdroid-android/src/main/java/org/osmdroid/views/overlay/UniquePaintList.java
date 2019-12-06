package org.osmdroid.views.overlay;

import android.graphics.Paint;

import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;

/**
 * Class UniquePaintList
 * @author Matthias Dittmer
 */
public class UniquePaintList implements PaintList {

    private Paint mPaint;
    private boolean mMonochromatic;
    private PolylineStyle mStyle;
    private int[] mColorIndexes;
    private float[] mLines;

    public UniquePaintList(Paint pPaint, boolean monochromatic, final PolylineStyle pStyle) {
        mPaint = pPaint;
        mMonochromatic = monochromatic;
        mStyle = pStyle;
    }

    @Override
    public void setReferencesForAdvancedStyling(final int[] pColorIndexes, final float[] pLines) {
        mColorIndexes = pColorIndexes;
        mLines = pLines;
    }

    @Override
    public Paint getPaint(int pIndex) {
        if(mStyle != null) {
            if(mMonochromatic) {
                return mPaint;
            } else {
                return mStyle.getPaintForLine(pIndex, mColorIndexes, mLines, mPaint);
            }
        } else {
            // index has no effect, just return the stored paint
            // paint object will not be modified (rounded joints ...)
            return mPaint;
        }
    }

    @Override
    public boolean isMonochromatic() {
        return mMonochromatic;
    }
}

package org.osmdroid.views.overlay;

import android.graphics.Paint;

import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;

/**
 * Class UniquePaintList holding the Paint and style information.
 * Used in LineDrawer class to get right Paint object.
 * @author Matthias Dittmer
 */
public class UniquePaintList implements PaintList {

    private Paint mPaint;
    private boolean mMonochromatic;
    private PolylineStyle mStyle;
    private int[] mColorIndexes;
    private float[] mLines;

    /**
     * Constructor
     * @param pPaint provide a paint object
     * @param monochromatic flag if line can be drawn at once
     * @param pStyle provide a style (optional)
     */
    public UniquePaintList(final Paint pPaint, final boolean monochromatic, final PolylineStyle pStyle) {
        mPaint = pPaint;
        mMonochromatic = monochromatic;
        mStyle = pStyle;
    }

    /**
     * Setup references for arrays.
     * This call is mandatory for non monochromatic lines with set style.
     * @param pColorIndexes reference to color index array
     * @param pLines reference to lines array
     */
    @Override
    public void setReferencesForAdvancedStyling(final int[] pColorIndexes, final float[] pLines) {
        mColorIndexes = pColorIndexes;
        mLines = pLines;
    }

    /**
     * Returns Paint for current index.
     * Please note: For monochromatic lines index as no effect. Pass zero.
     * Parameter index is only used for non monochromatic lines.
     * @param pIndex paint object
     * @return
     */
    @Override
    public Paint getPaint(final int pIndex) {
        if(mStyle != null) {
            // a style is set
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

    /**
     * Getter
     * @return monochromatic flag
     */
    @Override
    public boolean isMonochromatic() {
        return mMonochromatic;
    }
}

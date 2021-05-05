package org.osmdroid.views.overlay.advancedpolyline;

import android.graphics.Paint;

import org.osmdroid.views.overlay.PaintList;

/**
 * A {@link PaintList} with always the same color
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */
public class MonochromaticPaintList implements PaintList {

    private final Paint mPaint;

    public MonochromaticPaintList(final Paint pPaint) {
        mPaint = pPaint;
    }

    @Override
    public Paint getPaint() {
        return mPaint;
    }

    @Override
    public Paint getPaint(final int pIndex,
                          final float pX0, final float pY0, final float pX1, final float pY1) {
        return null;
    }
}

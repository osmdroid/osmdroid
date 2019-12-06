package org.osmdroid.views.overlay;

import android.graphics.Paint;

/**
 * Interface PaintList
 * @author Matthias Dittmer
 */
public interface PaintList {
    Paint getPaint(final int pIndex);
    void setReferencesForAdvancedStyling(final int[] pColorIndexes, final float[] pLines);
    boolean isMonochromatic();
}

package org.osmdroid.views.overlay;

import android.graphics.Paint;

/**
 * Interface PaintList
 *
 * @author Matthias Dittmer
 */
public interface PaintList {
    Paint getPaint();

    Paint getPaint(final int pIndex, final float pX0, final float pY0, final float pX1, final float pY1);
}

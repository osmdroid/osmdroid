package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.util.IntegerAccepter;
import org.osmdroid.util.LineBuilder;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;

/**
 * Created by Fabrice on 04/01/2018.
 *
 * @since 6.0.0
 */

public class LineDrawer extends LineBuilder {

    private IntegerAccepter mIntegerAccepter;
    private Canvas mCanvas;
    private PaintList mPaintList;

    public LineDrawer(int pMaxSize) {
        super(pMaxSize);
    }

    public void setCanvas(final Canvas pCanvas) {
        mCanvas = pCanvas;
    }

    public void setPaint(final Paint pPaint) {
        setPaint(new MonochromaticPaintList(pPaint));
    }

    public void setPaint(final PaintList pPaintList) {
        mPaintList = pPaintList;
    }

    public void setIntegerAccepter(final IntegerAccepter pIntegerAccepter) {
        mIntegerAccepter = pIntegerAccepter;
    }

    @Override
    public void flush() {
        final int nbSegments = getSize() / 4;
        if (nbSegments == 0) {
            additionalFlush();
            return;
        }
        final float[] lines = getLines();
        final Paint paint = mPaintList.getPaint();
        if (paint != null) { // monochromatic: that's enough
            final int size = compact(lines, nbSegments * 4);
            if (size > 0) {
                mCanvas.drawLines(lines, 0, size, paint);
            }
            additionalFlush();
            return;
        }
        for (int i = 0; i < nbSegments * 4; i += 4) {
            final float x0 = lines[i];
            final float y0 = lines[i + 1];
            final float x1 = lines[i + 2];
            final float y1 = lines[i + 3];
            if (x0 == x1 && y0 == y1) {
                continue;
            }
            final int segmentIndex = mIntegerAccepter.getValue(i / 2);
            mCanvas.drawLine(x0, y0, x1, y1, mPaintList.getPaint(segmentIndex, x0, y0, x1, y1));
        }
        additionalFlush();
    }

    private void additionalFlush() {
        if (mIntegerAccepter != null) {
            mIntegerAccepter.flush();
        }
    }

    /**
     * @param pLines the input AND output array
     * @param pSize  the initial number of coordinates
     * @return the number of relevant coordinates
     * @since 6.2.0
     * Compact a float[] containing (x0,y0,x1,y1) segment coordinate quadruplets
     * by removing the single point cases (x0 == x1 && y0 == y1)
     */
    private static int compact(final float[] pLines, final int pSize) {
        int dstIndex = 0;
        for (int srcIndex = 0; srcIndex < pSize; srcIndex += 4) {
            final float x0 = pLines[srcIndex];
            final float y0 = pLines[srcIndex + 1];
            final float x1 = pLines[srcIndex + 2];
            final float y1 = pLines[srcIndex + 3];
            if (x0 == x1 && y0 == y1) {
                continue;
            }
            if (srcIndex != dstIndex) {
                System.arraycopy(pLines, srcIndex, pLines, dstIndex, 4);
            }
            dstIndex += 4;
        }
        return dstIndex;
    }
}

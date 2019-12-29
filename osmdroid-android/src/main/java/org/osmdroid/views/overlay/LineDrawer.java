package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.util.IntegerAccepter;
import org.osmdroid.util.LineBuilder;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;

/**
 * Created by Fabrice on 04/01/2018.
 * @since 6.0.0
 */

public class LineDrawer extends LineBuilder{

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
        if(getSize() < 4) {
            additionalFlush();
            return;
        }
        final float[] lines = getLines();
        final Paint paint = mPaintList.getPaint();
        if (paint != null) { // monochromatic: that's enough
            mCanvas.drawLines(lines, 0, getSize(), paint);
            additionalFlush();
            return;
        }
        final int size = getSize();
        for (int i = 0; i < size ; i += 4) {
            final float x0 = lines[i];
            final float y0 = lines[i + 1];
            final float x1 = lines[i + 2];
            final float y1 = lines[i + 3];
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
}

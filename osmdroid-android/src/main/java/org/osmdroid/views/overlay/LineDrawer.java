package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.util.LineBuilder;

/**
 * Created by Fabrice on 04/01/2018.
 * @since 6.0.0
 */

public class LineDrawer extends LineBuilder{

    private Canvas mCanvas;
    private Paint mPaint;

    public LineDrawer(int pMaxSize) {
        super(pMaxSize);
    }

    public void setCanvas(final Canvas pCanvas) {
        mCanvas = pCanvas;
    }

    public void setPaint(final Paint pPaint) {
        mPaint = pPaint;
    }

    @Override
    public void flush() {
        if (getSize() >= 4) {
            mCanvas.drawLines(getLines(), 0, getSize(), mPaint);
        }
    }
}

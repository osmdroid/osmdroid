package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.util.LineBuilder;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingPlain;
import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;

/**
 * Created by Fabrice on 04/01/2018.
 * @since 6.0.0
 */

public class LineDrawer extends LineBuilder{

    private Canvas mCanvas;
    private Paint mPaint;
    private Paint mBorderPaint;
    private PolylineStyle mPolylineStyle = null;

    public LineDrawer(int pMaxSize) {
        super(pMaxSize);
    }

    public void setPolylineStyle(final PolylineStyle pStyle) {
        mPolylineStyle = pStyle;
    }

    public void setCanvas(final Canvas pCanvas) {
        mCanvas = pCanvas;
    }

    public void setPaint(final Paint pPaint) {
        mPaint = pPaint;
    }

    public void setBorderPaint(final Paint pBorderPaint) {
        mBorderPaint = pBorderPaint;
    }

    @Override
    public void flush() {
        if(getSize() < 4) {
            // nothing to draw, just return
            return;
        }
        // check for advanced styling
        if(mPolylineStyle != null) {
            // check for border and border mode enabled
            if(mBorderPaint != null) {
                // draw the complete polyline for the border
                mCanvas.drawLines(getLines(), 0, getSize(), mBorderPaint);
                return;
            }
            // check for monochromatic
            if (mPolylineStyle.isMonochromatic()) {
                // plain color can be drawn with one call, a set gradient as no effect
                mCanvas.drawLines(getLines(), 0, getSize(),
                        mPolylineStyle.getPaintForLine(0, getColorIndexes(), getLines(), mPaint));
                return;
            }
            // draw color mapping line segment by segment
            for (int i = 0; i < getSize() / 4; i++) {
                mCanvas.drawLines(getLines(), i * 4, 4,
                    mPolylineStyle.getPaintForLine(i, getColorIndexes(), getLines(), mPaint));
                }
            return;
        }

        // classic draw call without any style
        mCanvas.drawLines(getLines(), 0, getSize(), mPaint);
    }
}

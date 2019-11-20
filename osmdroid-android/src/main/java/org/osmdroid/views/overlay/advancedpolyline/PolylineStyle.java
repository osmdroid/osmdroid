package org.osmdroid.views.overlay.advancedpolyline;

import android.graphics.Paint;
import java.util.ArrayList;

/**
 * Polyline style holds color mapping, border style and gradient flag.
 * @author Matthias Dittmer
 */
public class PolylineStyle {

    /**
     * Local variables to describe current Polyline style.
     */
    private ColorMapping mColorMapping = null;
    private Paint mPaintBorder = null;
    private boolean mUseGradient = false;

    /**
     * Constructor
     * @param mapping
     * @param useGradient
     */
    public PolylineStyle(ColorMapping mapping, boolean useGradient) {
        mColorMapping = mapping;
        mUseGradient = useGradient;
    }

    /**
     * Call to set a border.
     * @param width this is the full width of the line. Should not be less than line itself (!).
     * @param color of the border.
     */
    public void setBorder(float width, int color) {
        // create the default border style once
        mPaintBorder = new Paint();
        mPaintBorder.setStyle(Paint.Style.STROKE);
        mPaintBorder.setAntiAlias(true);
        mPaintBorder.setStrokeJoin(Paint.Join.ROUND);
        mPaintBorder.setStrokeCap(Paint.Cap.ROUND);

        // set width and color
        mPaintBorder.setStrokeWidth(width);
        mPaintBorder.setColor(color);
    }

    /**
     * Call to unset a border.
     */
    public void unsetBorder() {
        mPaintBorder = null;
    }

    /**
     * Can be used to get current border paint.
     * @return will return null if border is not set. Otherwise final Paint object.
     */
    public final Paint getPaintBorder() {
        return mPaintBorder;
    }

    /**
     * Set a color mapping style.
     * @param mapping provide a color mapping (plain, cycle, ranges, variation)
     */
    public void setStyle(ColorMapping mapping) {
        mColorMapping = mapping;
    }

    /**
     * Get current
     * @return will return the current color mapping as final variable.
     */
    public final ColorMapping getStyle() {
        return mColorMapping;
    }

    /**
     * Do not call this function directly as library user.
     * @param scalar point scalar
     */
    public void addScalar(float scalar) {
        mColorMapping.addPoint(scalar);
    }

    /**
     * Do not call this function directly as library user.
     * @param scalarArray array of scalars for points.
     */
    public void setScalars(ArrayList<Float> scalarArray) {
        mColorMapping.setPoints(scalarArray);
    }

    /**
     * Get the complete list of scalars.
     * @return list of scalar as final variable.
     */
    public final ArrayList<Float> getScalars() {
        return mColorMapping.mScalarPerPoint;
    }

    /**
     * Modify paint object for current line.
     * @param paint
     * @return
     */
    public Paint getPaintForLine(int index, int[] colorIndexes, float[] lines, Paint paint) {

        // set rounded style
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // check for enabled gradient (has no effect for plain color)
        if(mUseGradient && !(mColorMapping.getClass() == ColorMappingPlain.class)) {
            paint.setShader(mColorMapping.createShader(colorIndexes[index],
                lines[index * 4], lines[index * 4 + 1], lines[index * 4 + 2], lines[index * 4 + 3]));
        } else {
            // reset shader
            paint.setShader(null);
            paint.setColor(mColorMapping.getColorForIndex(colorIndexes[index]));
        }

        // return modified paint object
        return paint;
    }
}

package org.osmdroid.views.overlay.advancedpolyline;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;

import java.util.ArrayList;

/**
 * Abstract base class for all implemented color mappings.
 * @author Matthias Dittmer
 */
public abstract class ColorMapping {

    /**
     * Two lists. One holding the scalar values. The other the calculated mapping.
     */
    protected ArrayList<Float> mScalarPerPoint = new ArrayList<>();
    protected ArrayList<Integer> mColorPerPoint = new ArrayList<>();

    /**
     * Get the color for the current index.
     * Function will return white if index is out of range.
     * @param index
     * @return color value
     */
    public Integer getColorForIndex(int index) {
        if(index <= mColorPerPoint.size() - 1) {
            return mColorPerPoint.get(index);
        } else {
            // index is out of range, return GRAY
            return Color.GRAY;
        }
    }

    /**
     * This function must be implemented by the specific color mapping class.
     * Add a new scalar is added at the end of of the list.
     * The scalar is used to set the line style from this point to the next.
     * @param scalar point scalar
     */
    public abstract void addPoint(float scalar);

    /**
     * Function to set a list of scalars at once. Clears old data.
     * @param scalarArray
     */
    public void setPoints(ArrayList<Float> scalarArray) {
        // clear old data
        mScalarPerPoint.clear();
        mColorPerPoint.clear();

        // just call addPoint for each list element
        for(Float scalar: scalarArray) {
            addPoint(scalar);
        }
    }

    /**
     * Create a shader for painting a line segment of the polyline.
     * @param index point index
     * @param x0 screen x coordinate start
     * @param y0 screen y coordinate start
     * @param x1 screen x coordinate end
     * @param y1 screen y coordinate end
     * @return LinearGradient in TileMode clamp
     */
    public Shader createShader(int index, float x0, float y0, float x1, float y1) {

        int startColor = mColorPerPoint.get(index);
        int endColor = mColorPerPoint.get(index + 1);

        // create a linear gradient
        return new LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP);
    }
}

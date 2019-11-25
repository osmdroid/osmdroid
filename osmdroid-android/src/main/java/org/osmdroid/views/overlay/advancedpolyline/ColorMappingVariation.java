package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Abstract base class for color variation mappings.
 * @author Matthias Dittmer
 */
public abstract class ColorMappingVariation extends ColorMapping{

    /**
     * All mapping variables.
     */
    private float mStart;
    private float mEnd;
    private float mScalarStart;
    private float mScalarEnd;
    private float mSlope;

    /**
     * Init function will be called from sub classes.
     * @param scalarStart start of scalar
     * @param scalarEnd end of scalar
     * @param start start of one HSL value
     * @param end end of one HSL value
     */
    public void init(final float scalarStart, final float scalarEnd,
         final float start, final float end) {

        mScalarStart = scalarStart;
        mScalarEnd = scalarEnd;
        mStart = start;
        mEnd = end;

        // calc slope once here for linear interpolation
        mSlope = (mEnd - mStart) / (mScalarEnd - mScalarStart);
    }

    /**
     * Map a scalar with clipping on lower and upper bound.
     * @param scalar
     * @return
     */
    public float mapScalar(final float scalar) {
        if(scalar >= mScalarEnd) {
            return mEnd;
        } else if(scalar <= mScalarStart) {
            return mStart;
        } else {
            // scalar is between start and end
            // do a linear mapping
            return scalar * mSlope + mStart;
        }
    }

    /**
     * Adds scalar to both lists.
     * @param scalar point scalar
     * @param h hue
     * @param s saturation
     * @param l luminance
     */
    public void addToLists(final float scalar, final float h, final float s, final float l) {
        float[] hsl = new float[] {h, s, l};
        mColorPerPoint.add(ColorHelper.HSLToColor(hsl));
        mScalarPerPoint.add(scalar);
    }
}

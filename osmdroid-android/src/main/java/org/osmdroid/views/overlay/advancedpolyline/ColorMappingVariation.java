package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Abstract base class for color variation mappings.
 *
 * @author Matthias Dittmer
 */
public abstract class ColorMappingVariation extends ColorMappingForScalar {

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
     *
     * @param scalarStart start of scalar
     * @param scalarEnd   end of scalar
     * @param start       start of one HSL value
     * @param end         end of one HSL value
     */
    public void init(final float scalarStart, final float scalarEnd,
                     final float start, final float end) {

        mScalarStart = scalarStart;
        mScalarEnd = scalarEnd;
        mStart = start;
        mEnd = end;

        // calc slope once here for linear interpolation
        mSlope = mScalarEnd == mScalarStart ? 1 : (mEnd - mStart) / (mScalarEnd - mScalarStart);
    }

    @Override
    protected int computeColor(final float pScalar) {
        return ColorHelper.HSLToColor(getHue(pScalar), getSaturation(pScalar), getLuminance(pScalar));
    }

    protected abstract float getHue(final float pScalar);

    protected abstract float getSaturation(final float pScalar);

    protected abstract float getLuminance(final float pScalar);

    /**
     * Map a scalar with clipping on lower and upper bound.
     */
    protected float mapScalar(float scalar) {
        if (scalar >= mScalarEnd) {
            return mEnd;
        } else if (scalar <= mScalarStart) {
            return mStart;
        } else {
            // scalar is between start and end
            // do a linear mapping
            return (scalar - mScalarStart) * mSlope + mStart;
        }
    }
}

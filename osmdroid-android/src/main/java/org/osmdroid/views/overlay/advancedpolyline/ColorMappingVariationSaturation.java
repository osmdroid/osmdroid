package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Color mapping for saturation variation.
 *
 * @author Matthias Dittmer
 */
public class ColorMappingVariationSaturation extends ColorMappingVariation {

    /**
     * Fixed HSL values.
     */
    private float mHue;
    private float mLuminance;

    /**
     * Constructor
     *
     * @param scalarStart     start of scalar
     * @param scalarEnd       end of scalar
     * @param saturationStart saturation start value
     * @param saturationEnd   saturation end value
     * @param hue             fixed hue value
     * @param luminance       fixed luminance value
     */
    public ColorMappingVariationSaturation(
            final float scalarStart, final float scalarEnd, float saturationStart, float saturationEnd,
            final float hue, final float luminance) {

        // do basic clipping for saturation value
        // please note: end can be lower than start for inverse mapping
        saturationStart = ColorHelper.constrain(saturationStart, 0.0f, 1.0f);
        saturationEnd = ColorHelper.constrain(saturationEnd, 0.0f, 1.0f);

        // do clipping for hue and luminance
        mHue = ColorHelper.constrain(hue, 0.0f, 360.0f);
        mLuminance = ColorHelper.constrain(luminance, 0.0f, 1.0f);

        init(scalarStart, scalarEnd, saturationStart, saturationEnd);
    }

    @Override
    protected float getHue(final float pScalar) {
        return mHue;
    }

    @Override
    protected float getSaturation(final float pScalar) {
        return mapScalar(pScalar);
    }

    @Override
    protected float getLuminance(final float pScalar) {
        return mLuminance;
    }
}

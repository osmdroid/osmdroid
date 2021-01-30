package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Color mapping for hue variation.
 *
 * @author Matthias Dittmer
 */
public class ColorMappingVariationHue extends ColorMappingVariation {

    /**
     * Fixed HSL values.
     */
    private float mSaturation;
    private float mLuminance;

    /**
     * Constructor
     *
     * @param scalarStart start of scalar
     * @param scalarEnd   end of scalar
     * @param hueStart    hue start value
     * @param hueEnd      hue end value
     * @param saturation  fixed saturation value
     * @param luminance   fixed luminance value
     */
    public ColorMappingVariationHue(
            final float scalarStart, final float scalarEnd, float hueStart, float hueEnd,
            final float saturation, final float luminance) {

        // do basic clipping for hue value
        // please note: end can be lower than start for inverse mapping
        hueStart = ColorHelper.constrain(hueStart, 0.0f, 360.0f);
        hueEnd = ColorHelper.constrain(hueEnd, 0.0f, 360.0f);

        // do clipping for saturation and luminance
        mSaturation = ColorHelper.constrain(saturation, 0.0f, 1.0f);
        mLuminance = ColorHelper.constrain(luminance, 0.0f, 1.0f);

        init(scalarStart, scalarEnd, hueStart, hueEnd);
    }

    @Override
    protected float getHue(final float pScalar) {
        return mapScalar(pScalar);
    }

    @Override
    protected float getSaturation(final float pScalar) {
        return mSaturation;
    }

    @Override
    protected float getLuminance(final float pScalar) {
        return mLuminance;
    }
}

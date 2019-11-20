package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping for saturation variation.
 * @author Matthias Dittmer
 */
public class ColorMappingVariationSaturation extends ColorMappingVariation{

    private float mHue;
    private float mLuminance;

    public ColorMappingVariationSaturation(float scalarStart, float scalarEnd,
                                    float saturationStart, float saturationEnd,
                                           float hue, float luminance) {

        // do basic clipping for hue value
        // please note: end can be lower than start for inverse mapping
        saturationStart = ColorHelper.constrain(saturationStart, 0.0f, 1.0f);
        saturationEnd = ColorHelper.constrain(saturationEnd, 0.0f, 1.0f);

        // do clipping for hue and luminance
        mHue = ColorHelper.constrain(hue, 0.0f, 360.0f);
        mLuminance = ColorHelper.constrain(luminance, 0.0f, 1.0f);

        super.init(scalarStart, scalarEnd, saturationStart, saturationEnd);
    }

    public void addPoint(float scalar) {
        // create mapped saturation value
        super.addToLists(scalar, mHue, mapScalar(scalar), mLuminance);
    }
}

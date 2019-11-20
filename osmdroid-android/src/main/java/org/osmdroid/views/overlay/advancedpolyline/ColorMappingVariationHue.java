package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping for hue variation.
 * @author Matthias Dittmer
 */
public class ColorMappingVariationHue extends ColorMappingVariation {

    private float mSaturation;
    private float mLuminance;

    public ColorMappingVariationHue(float scalarStart, float scalarEnd,
        float hueStart, float hueEnd, float saturation, float luminance) {

        // do basic clipping for hue value
        // please note: end can be lower than start for inverse mapping
        hueStart = ColorHelper.constrain(hueStart, 0.0f, 360.0f);
        hueEnd = ColorHelper.constrain(hueEnd, 0.0f, 360.0f);

        // do clipping for saturation and luminance
        mSaturation = ColorHelper.constrain(saturation, 0.0f, 1.0f);
        mLuminance = ColorHelper.constrain(luminance, 0.0f, 1.0f);

        super.init(scalarStart, scalarEnd, hueStart, hueEnd);
    }

    public void addPoint(float scalar) {
        // create mapped hue value
        super.addToLists(scalar, mapScalar(scalar), mSaturation, mLuminance);
    }
}

package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping for luminance variation.
 * @author Matthias Dittmer
 */
public class ColorMappingVariationLuminance extends ColorMappingVariation {

    private float mHue;
    private float mSaturation;

    public ColorMappingVariationLuminance(float scalarStart, float scalarEnd,
                                           float luminanceStart, float luminanceEnd,
                                           float hue, float saturation) {

        // do basic clipping for hue value
        // please note: end can be lower than start for inverse mapping
        luminanceStart = ColorHelper.constrain(luminanceStart, 0.0f, 1.0f);
        luminanceEnd = ColorHelper.constrain(luminanceEnd, 0.0f, 1.0f);

        // do clipping for hue and luminance
        mHue = ColorHelper.constrain(hue, 0.0f, 360.0f);
        mSaturation = ColorHelper.constrain(saturation, 0.0f, 1.0f);

        super.init(scalarStart, scalarEnd, luminanceStart, luminanceEnd);
    }

    public void addPoint(float scalar) {
        // create mapped luminance value
        super.addToLists(scalar, mHue, mSaturation, mapScalar(scalar));
    }
}

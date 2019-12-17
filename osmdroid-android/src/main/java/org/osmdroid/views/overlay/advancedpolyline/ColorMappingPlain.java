package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Color mapping with just one color. Not really a mapping.
 * Can be used to display a plain colored line with border.
 * If no border is set you can just use the classic polyline without any style.
 * @author Matthias Dittmer
 */
public class ColorMappingPlain implements ColorMapping {

    /**
     * Line color
     */
    private final int mColorPlain;

    public ColorMappingPlain(final int color) {
        mColorPlain = color;
    }

    @Override
    public int getColorForIndex(final int pSegmentIndex) {
        return mColorPlain;
    }
}

package org.osmdroid.views.overlay.advancedpolyline;

import java.util.List;

/**
 * Color mapping to cycle through an array of colors.
 *
 * @author Matthias Dittmer
 */
public class ColorMappingCycle implements ColorMapping {

    private final List<Integer> mColorList;
    private final int[] mColorArray;
    private int mGeoPointNumber;

    public ColorMappingCycle(final List<Integer> pColors) {
        mColorList = pColors;
        mColorArray = null;
    }

    public ColorMappingCycle(final int[] pColors) {
        mColorList = null;
        mColorArray = pColors;
    }

    /**
     * Ignore most of the time.
     * Only useful if you display a closed polyline with gradients:
     * * when displaying a segment with a gradient,
     * you compute the gradient from the current segment's color to the next segment's color
     * * in the closing segment case, we compute normally the color of the last segment,
     * but we also need to know the color of the next segment, which is the very first segment
     * That's why we need to know the number of segments.
     * Without that information, we would just give the next color of the cycle.
     *
     * @param pGeoPointNumber Number of GeoPoints of the polyline
     */
    public void setGeoPointNumber(final int pGeoPointNumber) {
        mGeoPointNumber = pGeoPointNumber;
    }

    @Override
    public int getColorForIndex(int pSegmentIndex) {
        if (mGeoPointNumber > 0 && pSegmentIndex >= mGeoPointNumber) {
            pSegmentIndex = 0;
        }
        if (mColorArray != null) {
            return mColorArray[pSegmentIndex % mColorArray.length];
        }
        if (mColorList != null) {
            return mColorList.get(pSegmentIndex % mColorList.size());
        }
        throw new IllegalArgumentException();
    }
}

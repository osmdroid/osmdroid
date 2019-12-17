package org.osmdroid.views.overlay.advancedpolyline;

import java.util.Map;
import java.util.SortedMap;

/**
 * Color mapping to map ranges to specific colors.
 * @author Matthias Dittmer
 */
public class ColorMappingRanges extends ColorMappingForScalar {

    /**
     * Using a sorted map to define borders of ranges.
     * Borders are sorted from low to high.
     */
    private SortedMap<Float, Integer> mColorRanges;

    public ColorMappingRanges(final SortedMap<Float, Integer> colorArray) {
        mColorRanges = colorArray;
    }

    @Override
    protected int computeColor(final float pScalar) {
        int lastArrayIndexFromLoop = 0;
        // iterate over array and sort point in
        for (Map.Entry<Float, Integer> entry : mColorRanges.entrySet()) {

            if(pScalar < entry.getKey()) {
                return entry.getValue();
            }
            lastArrayIndexFromLoop++;

        }
        // assign last color if scalar is above highest border
        if(lastArrayIndexFromLoop == mColorRanges.size()) {
            return mColorRanges.get(mColorRanges.lastKey());
        }
        return 0;
    }
}

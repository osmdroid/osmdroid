package org.osmdroid.views.overlay.advancedpolyline;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Color mapping to map ranges to specific colors.
 * @author Matthias Dittmer
 */
public class ColorMappingRanges extends ColorMapping {

    /**
     * Using a sorted map to define borders of ranges.
     * Borders are sorted from low to high.
     */
    private SortedMap<Float, Integer> mColorRanges;

    /**
     * Constructor
     * @param colorArray
     */
    public ColorMappingRanges(final SortedMap<Float, Integer> colorArray) {
        mColorRanges = colorArray;
    }

    /**
     * Add a point.
     * @param scalar for point
     */
    public void addPoint(final float scalar) {
        int lastArrayIndexFromLoop = 0;
        // iterate over array and sort point in
        for (Map.Entry<Float, Integer> entry : mColorRanges.entrySet()) {

            if(scalar < entry.getKey()) {
                mColorPerPoint.add(entry.getValue());
                // leave loop
                break;
            }
            lastArrayIndexFromLoop++;

        }
        // assign last color if scalar is above highest border
        if(lastArrayIndexFromLoop == mColorRanges.size()) {
            mColorPerPoint.add(mColorRanges.get(mColorRanges.lastKey()));
        }
    }
}

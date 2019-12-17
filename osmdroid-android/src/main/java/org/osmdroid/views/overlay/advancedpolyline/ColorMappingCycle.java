package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping to cycle through an array of colors.
 * @author Matthias Dittmer
 */
public class ColorMappingCycle implements ColorMapping {

    /**
     * Color array
     */
    private ArrayList<Integer> mColorArray;

    /**
     * Constructor
     * @param colorArray array holding the colors
     */
    public ColorMappingCycle(final ArrayList<Integer> colorArray) {
        mColorArray = colorArray;
    }

    @Override
    public int getColorForIndex(int pSegmentIndex) {
        return mColorArray.get(pSegmentIndex % mColorArray.size());
    }
}

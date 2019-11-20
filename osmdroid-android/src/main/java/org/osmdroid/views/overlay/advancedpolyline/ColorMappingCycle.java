package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping to cycle through an array of colors.
 * @author Matthias Dittmer
 */
public class ColorMappingCycle extends ColorMapping {

    /**
     * Color array
     */
    private ArrayList<Integer> mColorArray;

    /**
     * Constructor
     * @param colorArray array holding the colors
     */
    public ColorMappingCycle(ArrayList<Integer> colorArray) {
        mColorArray = colorArray;
    }

    /**
     * Add a point.
     * @param scalar
     */
    public void addPoint(float scalar) {
        mScalarPerPoint.add(scalar);
        mColorPerPoint.add(mColorArray.get(mScalarPerPoint.size() % mColorArray.size()));
    }
}
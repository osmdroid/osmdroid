package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;

/**
 * Color mapping with just one color. Not really a mapping.
 * Can be used to display a plain colored line with border.
 * If no border is set you can just use the classic polyline without any style.
 * @author Matthias Dittmer
 */
public class ColorMappingPlain extends ColorMapping {

    /**
     * Line color
     */
    private Integer mColorPlain;

    /**
     * Constructor
     * @param color
     */
    public ColorMappingPlain(Integer color) {
        mColorPlain = color;
    }

    /**
     * Add a point.
     * @param scalar
     */
    public void addPoint(float scalar) {
        mScalarPerPoint.add(scalar);
        mColorPerPoint.add(mColorPlain);
    }
}

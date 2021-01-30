package org.osmdroid.samplefragments.drawing;

/**
 * created on 26/12/2017.
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class DrawPolygonWithArrows extends DrawPolygon {

    @Override
    public String getSampleTitle() {
        return "Draw a polygon with arrows";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        paint.withArrows = true;
    }
}

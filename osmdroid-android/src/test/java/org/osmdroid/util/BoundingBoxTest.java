package org.osmdroid.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.osmdroid.views.MapView;

/**
 * @since 6.0.0
 * @author Fabrice Fontaine
 * @author Andreas Schildbach
 */

public class BoundingBoxTest {

    private static final double TOLERANCE = 1E-5;

    @Test
    public void testGetCenterLongitude() {
        assertEquals(1.5, BoundingBox.getCenterLongitude(1, 2), TOLERANCE);
        assertEquals(-178.5, BoundingBox.getCenterLongitude(2, 1), TOLERANCE);
    }

    @Test
    public void getSpansWithoutDateLine() {
        BoundingBox bb = new BoundingBox(10, 10, -10, -10);
        assertEquals(20, bb.getLongitudeSpanWithDateLine(), TOLERANCE);
        assertEquals(20, bb.getLongitudeSpan(), TOLERANCE);
        assertEquals(20, bb.getLatitudeSpan(), TOLERANCE);
    }

    @Test
    public void getSpansWithDateLine() {
        BoundingBox bb = new BoundingBox(10, -170, -10, 170);
        assertEquals(20, bb.getLongitudeSpanWithDateLine(), TOLERANCE);
        assertEquals(20, bb.getLatitudeSpan(), TOLERANCE);
        bb = new BoundingBox(10, -10, -10, 10);
        assertEquals(340, bb.getLongitudeSpanWithDateLine(), TOLERANCE);
    }

    @Test
    public void increaseByScale() {
        BoundingBox bb = new BoundingBox(10, 20, 0, 0).increaseByScale(1.2f);
        assertEquals(11, bb.getLatNorth(), TOLERANCE);
        assertEquals(22, bb.getLonEast(), TOLERANCE);
        assertEquals(-1, bb.getLatSouth(), TOLERANCE);
        assertEquals(-2, bb.getLonWest(), TOLERANCE);
    }

    @Test
    public void increaseByScale_onDateLine() {
        BoundingBox bb = new BoundingBox(10, -170, -10, 170).increaseByScale(1.2f);
        assertEquals(12, bb.getLatNorth(), TOLERANCE);
        assertEquals(-168, bb.getLonEast(), TOLERANCE);
        assertEquals(-12, bb.getLatSouth(), TOLERANCE);
        assertEquals(168, bb.getLonWest(), TOLERANCE);
    }

    @Test
    public void increaseByScale_clipNorth() {
        BoundingBox bb = new BoundingBox(80, 20, 0, -20).increaseByScale(1.2f);
        assertEquals(MapView.getTileSystem().getMaxLatitude(), bb.getLatNorth(), TOLERANCE);
        assertEquals(-8, bb.getLatSouth(), TOLERANCE);
    }

    @Test
    public void increaseByScale_clipSouth() {
        BoundingBox bb = new BoundingBox(0, 20, -80, -20).increaseByScale(1.2f);
        assertEquals(8, bb.getLatNorth(), TOLERANCE);
        assertEquals(MapView.getTileSystem().getMinLatitude(), bb.getLatSouth(), TOLERANCE);
    }

    @Test
    public void increaseByScale_wrapEast() {
        BoundingBox bb = new BoundingBox(20, 175, -20, 75).increaseByScale(1.2f);
        assertEquals(-175, bb.getLonEast(), TOLERANCE);
        assertEquals(65, bb.getLonWest(), TOLERANCE);
    }

    @Test
    public void increaseByScale_wrapWest() {
        BoundingBox bb = new BoundingBox(20, -75, -20, -175).increaseByScale(1.2f);
        assertEquals(-65, bb.getLonEast(), TOLERANCE);
        assertEquals(175, bb.getLonWest(), TOLERANCE);
    }
}

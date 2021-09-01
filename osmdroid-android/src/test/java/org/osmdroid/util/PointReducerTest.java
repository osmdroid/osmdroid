package org.osmdroid.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * created on 2/3/2018.
 *
 * @author Alex O'Ree
 */

public class PointReducerTest {

    @Test
    public void testReducer() {
        ArrayList<GeoPoint> pts = new ArrayList<>();
        pts.add(new GeoPoint(45, -74.0));
        pts.add(new GeoPoint(45.0009, -74.0009));   //about 1km
        pts.add(new GeoPoint(45.0018, -74.0018));   //about 1km

        ArrayList<GeoPoint> geoPoints = PointReducer.reduceWithTolerance(pts, 0.5 / 312);   //about 50km latitude span
        Assert.assertTrue(!geoPoints.isEmpty());
        Assert.assertTrue(geoPoints.size() + "", geoPoints.size() == 2);
    }
}

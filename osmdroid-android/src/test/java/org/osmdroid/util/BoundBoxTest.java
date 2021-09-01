package org.osmdroid.util;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 9/11/16.
 */

public class BoundBoxTest {

    private static final TileSystem tileSystem = new TileSystemWebMercator();

    @Test
    public void testBoundingBox() {

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(1d, 1d));
        partialPolyLine.add(new GeoPoint(1d, -1d));
        partialPolyLine.add(new GeoPoint(-1d, 1d));
        partialPolyLine.add(new GeoPoint(-1d, -1d));
        partialPolyLine.add(new GeoPoint(0d, 0d));
        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLatitude(), 0d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLongitude(), 0d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(), 1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(), -1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(), 1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(), -1d, 0.000001d);
    }

    @Test
    public void testBoundingBoxMax() {

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(tileSystem.getMaxLatitude(), 180d));
        partialPolyLine.add(new GeoPoint(tileSystem.getMinLatitude(), -180d));

        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLatitude(), 0d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLongitude(), 0d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(), tileSystem.getMaxLatitude(), 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(), tileSystem.getMinLatitude(), 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(), 180d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(), -180d, 0.000001d);
    }


    @Test
    public void testBoundingBoxAllNegs() {

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(-46d, -46d));
        partialPolyLine.add(new GeoPoint(-45d, -45d));

        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLatitude(), -45.5d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenterWithDateLine().getLongitude(), -45.5d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(), -45d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(), -46d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(), -45d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(), -46d, 0.000001d);
    }

    @Test
    public void testBoundingBoxIrregular() {
        List<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(27.821134999999998, -97.21217899999999));
        points.add(new GeoPoint(27.822409999999998, -97.211607));
        points.add(new GeoPoint(27.835423, -97.20577));
        points.add(new GeoPoint(27.837301, -97.204944));
        points.add(new GeoPoint(27.837668999999998, -97.204782));
        points.add(new GeoPoint(27.838047, -97.204616));
        points.add(new GeoPoint(27.838178, -97.19545699999999));
        points.add(new GeoPoint(27.838185, -97.194859));
        points.add(new GeoPoint(27.838179, -97.19440399999999));
        points.add(new GeoPoint(27.838168, -97.194245));
        points.add(new GeoPoint(27.838165999999998, -97.194212));
        points.add(new GeoPoint(27.838148999999998, -97.194105));
        points.add(new GeoPoint(27.838144, -97.194086));
        points.add(new GeoPoint(27.838071, -97.19375699999999));
        points.add(new GeoPoint(27.838037999999997, -97.19363799999999));
        points.add(new GeoPoint(27.838030999999997, -97.193619));
        points.add(new GeoPoint(27.837996999999998, -97.193512));
        points.add(new GeoPoint(27.837979999999998, -97.193468));
        points.add(new GeoPoint(27.837951999999998, -97.19339699999999));
        points.add(new GeoPoint(27.837901, -97.19326699999999));
        points.add(new GeoPoint(27.837878999999997, -97.19318299999999));
        points.add(new GeoPoint(27.83786, -97.193111));
        points.add(new GeoPoint(27.837595, -97.19321));
        points.add(new GeoPoint(27.836557, -97.19368999999999));
        points.add(new GeoPoint(27.836017, -97.193941));
        points.add(new GeoPoint(27.834646, -97.194563));
        points.add(new GeoPoint(27.833799, -97.19493899999999));
        points.add(new GeoPoint(27.832649999999997, -97.19543399999999));
        points.add(new GeoPoint(27.832535, -97.195484));
        points.add(new GeoPoint(27.832310999999997, -97.195588));
        points.add(new GeoPoint(27.831644999999998, -97.195914));
        points.add(new GeoPoint(27.831421, -97.196018));
        points.add(new GeoPoint(27.831360999999998, -97.19604299999999));
        points.add(new GeoPoint(27.831025999999998, -97.19621));
        points.add(new GeoPoint(27.830997999999997, -97.196225));
        points.add(new GeoPoint(27.830274, -97.196467));
        points.add(new GeoPoint(27.829973, -97.196595));
        points.add(new GeoPoint(27.829829999999998, -97.196657));
        points.add(new GeoPoint(27.829731, -97.19667899999999));
        points.add(new GeoPoint(27.829615999999998, -97.196699));
        points.add(new GeoPoint(27.829829, -97.19760199999999));
        points.add(new GeoPoint(27.829442999999998, -97.197783));
        points.add(new GeoPoint(27.829482, -97.19800000000001));

        BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);

        Assert.assertEquals(boundingBox.getLatNorth(), 27.838185, 0.00001);
        Assert.assertEquals(boundingBox.getLatSouth(), 27.821134, 0.00001);

        Assert.assertEquals(boundingBox.getLonEast(), -97.193111, 0.00001);
        Assert.assertEquals(boundingBox.getLonWest(), -97.212178, 0.00001);
    }

}

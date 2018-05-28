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
    public void testBoundingBox() throws Exception{

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(1d,1d));
        partialPolyLine.add(new GeoPoint(1d,-1d));
        partialPolyLine.add(new GeoPoint(-1d,1d));
        partialPolyLine.add(new GeoPoint(-1d, -1d));
        partialPolyLine.add(new GeoPoint(0d, 0d));
        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenter().getLatitude(),0d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenter().getLongitude(),0d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(),1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(),-1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(),1d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(),-1d, 0.000001d);
    }

    @Test
    public void testBoundingBoxMax() throws Exception{

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(tileSystem.getMaxLatitude(),180d));
        partialPolyLine.add(new GeoPoint(tileSystem.getMinLatitude(),-180d));

        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenter().getLatitude(),0d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenter().getLongitude(),0d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(),tileSystem.getMaxLatitude(), 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(),tileSystem.getMinLatitude(), 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(),180d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(),-180d, 0.000001d);
    }


    @Test
    public void testBoundingBoxAllNegs() throws Exception{

        List<IGeoPoint> partialPolyLine = new ArrayList<>();
        partialPolyLine.add(new GeoPoint(-46d,-46d));
        partialPolyLine.add(new GeoPoint(-45d,-45d));

        BoundingBox fromGeoPoints = BoundingBox.fromGeoPoints(partialPolyLine);
        Assert.assertEquals(fromGeoPoints.getCenter().getLatitude(),-45.5d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getCenter().getLongitude(),-45.5d, 0.000001d);


        Assert.assertEquals(fromGeoPoints.getLatNorth(),-45d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLatSouth(),-46d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonEast(),-45d, 0.000001d);
        Assert.assertEquals(fromGeoPoints.getLonWest(),-46d, 0.000001d);
    }
}

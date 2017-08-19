package org.osmdroid.gpkg.features;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

public class PolygonOptions {
    private List<GeoPoint> points=new ArrayList<>();
    private List<List<GeoPoint>> holes;

    public List<GeoPoint> getPoints() {
        return points;
    }

    public List<List<GeoPoint>> getHoles() {
        return holes;
    }

    public void add(GeoPoint latLng) {

        points.add(latLng);

    }

    public void addHole(List<GeoPoint> holeLatLngs) {
       // holes.addAll(holeLatLngs);
        //FIXME
    }

    public void zIndex(float v) {
//FIXME
    }
}

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

public class PolylineOptions {
    private List<GeoPoint> points=new ArrayList<>();

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void zIndex(float v) {
//FIXME
    }

    public void add(GeoPoint latLng) {

        points.add(latLng);

    }
}

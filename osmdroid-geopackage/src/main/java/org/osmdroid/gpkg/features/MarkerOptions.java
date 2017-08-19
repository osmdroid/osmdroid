package org.osmdroid.gpkg.features;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

public class MarkerOptions {
    protected GeoPoint position;
    private Object icon;

    public GeoPoint getPosition() {
        return position;
    }

    public Object getIcon() {
        return icon;
    }
}

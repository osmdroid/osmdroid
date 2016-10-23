package org.osmdroid.samplefragments.models;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by alex on 10/20/16.
 */

public class MyMapItem extends OverlayItem {
    public MyMapItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);
    }
}

package org.osmdroid.samplefragments.models;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class SampleOverlayItem extends OverlayItem {

    public SampleOverlayItem(String aUid, String aTitle, String aDescription, GeoPoint aGeoPoint,
                             Drawable aMarker, HotspotPlace aHotspotPlace) {
        super(aUid, aTitle, aDescription, aGeoPoint);
        this.setMarker(aMarker);
        this.setMarkerHotspot(aHotspotPlace);
    }

    public void draw(Canvas canvas) {
        //
    }

}

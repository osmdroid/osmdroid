package org.osmdroid.samplefragments.drawing;

/**
 * Map replication is OFF for this sample (only viewable for numerically lower zoom levels (higher altitude))
 * Created by Maradox on 11/26/17.
 */

public class DrawPolygonWithoutVerticalWrapping extends DrawPolygon {

    @Override
    public String getSampleTitle() {
        return "Draw a polygon on screen without vertical wrapping";
    }


    @Override
    public void addOverlays() {
        super.addOverlays();
        this.mMapView.setHorizontalMapRepetitionEnabled(true);
        this.mMapView.setVerticalMapRepetitionEnabled(false);
    }

}

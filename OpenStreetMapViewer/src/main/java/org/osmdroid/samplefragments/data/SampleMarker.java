package org.osmdroid.samplefragments.data;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

/**
 * An example on using osmbonuspack's Marker class by following the tutorial at
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0
 * https://github.com/MKergall/osmbonuspack/wiki/Tutorial_1
 *
 * created on 12/29/2016.
 *
 * @author Alex O'Ree
 */

public class SampleMarker extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Marker";
    }

    @Override
    public void addOverlays(){
        super.addOverlays();

        GeoPoint startPoint = new GeoPoint(38.8977, -77.0365);  //white house
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.icon));
        startMarker.setTitle("White House");
        startMarker.setSnippet("The White House is the official residence and principal workplace of the President of the United States.");
        startMarker.setSubDescription("1600 Pennsylvania Ave NW, Washington, DC 20500");
        mMapView.getOverlays().add(startMarker);
        mMapView.invalidate();
    }
}

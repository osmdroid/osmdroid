package org.osmdroid.samplefragments;

import android.util.Log;
import android.view.View;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

/**
 * A quick tutorial on how to listen for when the map is ready to go
 * Created by alex on 6/4/16.
 */
public class SampleMapBootListener extends BaseSampleFragment implements MapView.OnFirstLayoutListener {
    @Override
    public String getSampleTitle() {
        return "Start up events";
    }

    protected void addOverlays() {
        mMapView.addOnFirstLayoutListener(this);
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        Log.i("OsmBootUp", "onFirstLayout fired");
        mMapView.zoomToBoundingBox(new BoundingBoxE6(44d, -76d,43d, -77d), true);
    }


}

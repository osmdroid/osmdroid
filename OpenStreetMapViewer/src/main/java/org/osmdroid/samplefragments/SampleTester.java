package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import androidx.annotation.NonNull;

/**
 * Created by alex on 9/14/16.
 */

public class SampleTester extends BaseSampleFragment implements MapView.OnFirstLayoutListener {
    @Override
    public String getSampleTitle() {
        return "Alex's Tester";
    }

    protected void addOverlays() {
        mCompassOverlay = new CompassOverlay(mMapView, new InternalCompassOrientationProvider(mMapView));
        mCompassOverlay.enableCompass();
        mMapView.getOverlayManager().add(this.mCompassOverlay);
    }

    private CompassOverlay mCompassOverlay = null;

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        Log.i("OsmBootUp", "onFirstLayout fired");
        mMapView.zoomToBoundingBox(new BoundingBox(44d, -76d, 43d, -77d), true);

    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.addOnFirstLayoutListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCompassOverlay != null) this.mCompassOverlay.disableCompass();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCompassOverlay != null) {
            if (!this.mCompassOverlay.hasOrientationProvider()) this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(mMapView));
            this.mCompassOverlay.enableCompass();
        }

    }
}

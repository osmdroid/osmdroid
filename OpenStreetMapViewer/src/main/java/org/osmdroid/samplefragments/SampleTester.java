package org.osmdroid.samplefragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

/**
 * Created by alex on 9/14/16.
 */

public class SampleTester extends BaseSampleFragment implements MapView.OnFirstLayoutListener {
    @Override
    public String getSampleTitle() {
        return "Alex's Tester";
    }

    protected void addOverlays() {
        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
        mCompassOverlay = new CompassOverlay(getContext(), new InternalCompassOrientationProvider(getContext()),
                mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(this.mCompassOverlay);
    }

    private CompassOverlay mCompassOverlay = null;

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        Log.i("OsmBootUp", "onFirstLayout fired");
        mMapView.zoomToBoundingBox(new BoundingBox(44d, -76d, 43d, -77d), true);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.addOnFirstLayoutListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
        if (mCompassOverlay != null) {
            this.mCompassOverlay.disableCompass();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //sorry for the spaghetti code this is to filter out the compass on api 8
        //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218

        if (mCompassOverlay != null) {
            //this call is needed because onPause, the orientation provider is destroyed to prevent context leaks
            this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(getActivity()));
            this.mCompassOverlay.enableCompass();
        }

    }
}

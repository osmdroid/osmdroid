package org.osmdroid.samplefragments.tilesources;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.CopyrightOverlay;

/**
 * creates an in your face, ugly, copyright banner
 * created on 1/3/2017.
 *
 * @author Alex O'Ree
 */

public class SampleCopyrightOverlay extends BaseSampleFragment {


    @Override
    public String getSampleTitle() {
        return "Copyright with offsets";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        mMapView.getOverlays().clear();
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getActivity());
        copyrightOverlay.setTextColor(Color.GREEN);
        copyrightOverlay.setTextSize(20);
        copyrightOverlay.setAlignBottom(true);
        copyrightOverlay.setAlignRight(false);
        copyrightOverlay.setOffset(20, 40);
        //with align bottom and left, this should be 20dp from the bottom, 20dp from the left

        mMapView.getOverlays().add(copyrightOverlay);

    }
}

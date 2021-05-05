package org.osmdroid.samplefragments.layouts;

import android.content.Context;
import android.util.DisplayMetrics;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.ScaleBarOverlay;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class ScaleBarOnBottom extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Scale Bar on the bottom";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        final Context context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mMapView);

        scaleBarOverlay.setCentred(true);

        //api15 and up, 85 is right at the bottom
        //we are also adding 20dp padding for the overlay overlay which is added by the super class
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, dm.heightPixels - (int) (105 * dm.density));

        scaleBarOverlay.setUnitsOfMeasure(ScaleBarOverlay.UnitsOfMeasure.imperial);
        mMapView.getOverlayManager().add(scaleBarOverlay);
    }
}

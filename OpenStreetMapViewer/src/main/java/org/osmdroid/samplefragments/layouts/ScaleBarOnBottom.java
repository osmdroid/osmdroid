package org.osmdroid.samplefragments.layouts;

import android.content.Context;
import android.os.Build;
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
    public void addOverlays(){
        super.addOverlays();
        final Context context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mMapView);

        scaleBarOverlay.setCentred(true);

        //i hate this very much, but it seems as if certain versions of android and/or
        //device types handle screen offsets differently
        //60 on api10 is right at the bottom
        //api15 and up, 85 is right at the bottom
        //we are also adding 20dp padding for the overlay overlay which is added by the super class
        if (Build.VERSION.SDK_INT <= 10)
            scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, dm.heightPixels - (int)(80*dm.density));
        else

            scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, dm.heightPixels - (int)(105*dm.density));

        scaleBarOverlay.setUnitsOfMeasure(ScaleBarOverlay.UnitsOfMeasure.imperial);
        mMapView.getOverlayManager().add(scaleBarOverlay);
    }
}

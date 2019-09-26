package org.osmdroid.samplefragments.drawing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @since 6.1.0
 * @author Fabrice Fontaine
 */

public class SampleDrawPolylineAsPath extends SampleDrawPolyline {

    @Override
    public String getSampleTitle() {
        return "Draw a polyline on screen as Path";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = super.onCreateView(inflater, container, savedInstanceState);
        paint.setMode(CustomPaintingSurface.Mode.PolylineAsPath);
        return result;
    }
}

package org.osmdroid.samplefragments.tilesources;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * created on 1/8/2017.
 *
 * @author Alex O'Ree
 */

public class SepiaToneTiles extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Sepia tone tiles";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        final ColorMatrix matrixA = new ColorMatrix();
        // making image B&W
        matrixA.setSaturation(0);

        final ColorMatrix matrixB = new ColorMatrix();
        // applying scales for RGB color values
        matrixB.setScale(1f, .95f, .82f, 1.0f);
        matrixA.setConcat(matrixB, matrixA);

        final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixA);

        mMapView.getOverlayManager().getTilesOverlay().setColorFilter(filter);

    }
}

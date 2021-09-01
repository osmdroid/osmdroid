package org.osmdroid.samplefragments.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleAnimateToWithOrientation extends BaseSampleFragment implements View.OnClickListener {

    private static final float[] ORIENTATIONS = new float[]{30, 0, -30, 0, -30, 0};
    private static final Boolean[] CLOCKWISES = new Boolean[]{null, null, null, null, Boolean.TRUE, Boolean.FALSE};

    private final GeoPoint MAP_CENTER = new GeoPoint(0., 0);
    private int mIndex = -1;
    private String mLabel;

    @Override
    public String getSampleTitle() {
        return "Animate To With Orientation";
    }

    private Button btnCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        next();

/*        final RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mMapView);
        rotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(rotationGestureOverlay);
*/
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:
                mMapView.getController().animateTo(MAP_CENTER, null, null, ORIENTATIONS[mIndex], CLOCKWISES[mIndex]);
                next();
                break;
        }
    }

    private void next() {
        mIndex++;
        mIndex %= ORIENTATIONS.length;
        mLabel = "To " + ORIENTATIONS[mIndex] + " " +
                (CLOCKWISES[mIndex] == null ? "" : CLOCKWISES[mIndex] ? "clockwise" : "anticlockwise");
        btnCache.setText(mLabel);
    }
}

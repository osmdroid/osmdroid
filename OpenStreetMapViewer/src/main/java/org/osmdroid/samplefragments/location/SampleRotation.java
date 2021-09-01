package org.osmdroid.samplefragments.location;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

/**
 * created on 1/2/2017.
 *
 * @author Alex O'Ree
 */

public class SampleRotation extends BaseSampleFragment implements View.OnClickListener {
    ImageButton btnRotateLeft, btnRotateRight;
    protected TextView textViewCurrentLocation = null;

    @Override
    public String getSampleTitle() {
        return "Map Rotation";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.map_with_locationbox_controls, null);
        mMapView = root.findViewById(R.id.mapview);
        btnRotateLeft = root.findViewById(R.id.btnRotateLeft);
        btnRotateLeft.setOnClickListener(this);
        btnRotateRight = root.findViewById(R.id.btnRotateRight);
        btnRotateRight.setOnClickListener(this);
        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);
        textViewCurrentLocation.setText("0.0");
        return root;

    }

    @Override
    public void addOverlays() {
        super.addOverlays();

        final DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(mRotationGestureOverlay);

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setScaleBarOffset(0, (int) (40 * dm.density));
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mMapView.getOverlays().add(mScaleBarOverlay);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRotateLeft: {
                float angle = mMapView.getMapOrientation() + 10;
                if (angle > 360)
                    angle = 360 - angle;
                mMapView.setMapOrientation(angle);
            }
            break;
            case R.id.btnRotateRight: {
                float angle = mMapView.getMapOrientation() - 10;
                if (angle < 0)
                    angle += 360f;
                mMapView.setMapOrientation(angle);
            }
            break;

        }
        textViewCurrentLocation.setText(mMapView.getMapOrientation() + "");
    }
}

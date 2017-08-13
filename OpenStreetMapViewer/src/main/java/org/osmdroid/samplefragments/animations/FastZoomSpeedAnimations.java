package org.osmdroid.samplefragments.animations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.MapView;

/**
 * How to override animation speeds for zoom in/out<br>
 * Implementation notes:
 * <ul>
 *     <li>When using the build in zoom controls (android supplied, lower part of the view, the only way to override this speed is via preference. It
 *     is only checked when the mapview is created. Screen double tap to zoom is also affected by this. </li>
 *     <li>If using custom zoom in/out buttons, this can be changed using the example below.</li>
 * </ul>
 * created on 8/11/2017.
 *
 * @author Alex O'Ree
 */

public class FastZoomSpeedAnimations extends BaseSampleFragment implements View.OnClickListener {
    @Override
    public String getSampleTitle() {
        return "Super fast zoom speed";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox_controls, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        TextView textViewCurrentLocation = (TextView) root.findViewById(R.id.textViewCurrentLocation);
        textViewCurrentLocation.setText("Animation Speed Test");
        ImageButton btn = (ImageButton) root.findViewById(R.id.btnRotateLeft);
        btn.setOnClickListener(this);

        btn = (ImageButton) root.findViewById(R.id.btnRotateRight);
        btn.setOnClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //overrides the default animation speeds
        Configuration.getInstance().setAnimationSpeedShort(100);
        Configuration.getInstance().setAnimationSpeedDefault(100);
    }

    @Override
    public void onPause() {
        super.onPause();
        //maybe restore the old animation settings here
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRotateLeft:
                mMapView.getController().zoomIn(100L);
                break;
            case R.id.btnRotateRight:
                mMapView.getController().zoomOut(100L);
                break;
        }
    }
}

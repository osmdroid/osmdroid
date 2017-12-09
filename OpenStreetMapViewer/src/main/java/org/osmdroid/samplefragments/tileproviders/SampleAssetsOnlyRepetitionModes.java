package org.osmdroid.samplefragments.tileproviders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

/**
 * test for showing the map for different repetition modes
 * https://github.com/osmdroid/osmdroid/issues/183
 * Created by Maradox on 11/26/17.
 */
public class SampleAssetsOnlyRepetitionModes extends BaseSampleFragment {

    CheckBox horizontalCb;
    CheckBox verticalCb;
    CheckBox limitBoundsCb;

    @Override
    public String getSampleTitle() {
        return "Assets Only With Repetition Modes";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_wrapping, null);
        horizontalCb = (CheckBox) v.findViewById(R.id.horizontalRepetitionCb);
        verticalCb = (CheckBox) v.findViewById(R.id.verticalRepetitionCb);
        limitBoundsCb = (CheckBox) v.findViewById(R.id.limitBoundsCb);
        mMapView = (MapView) v.findViewById(org.osmdroid.R.id.mapview);

        horizontalCb.setChecked(true);
        verticalCb.setChecked(true);
        limitBoundsCb.setChecked(false);

        horizontalCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMapView.setHorizontalMapRepetitionEnabled(isChecked);
            }
        });

        verticalCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMapView.setVerticalMapRepetitionEnabled(isChecked);
            }
        });

        limitBoundsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    BoundingBox boundingBox = new BoundingBox(90, 180, -90, -180);
                    mMapView.setScrollableAreaLimitDouble(boundingBox);
                } else {
                    mMapView.setScrollableAreaLimitDouble(null);
                }
            }
        });

        return v;
    }
}

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
        horizontalCb = v.findViewById(R.id.horizontalRepetitionCb);
        verticalCb = v.findViewById(R.id.verticalRepetitionCb);
        limitBoundsCb = v.findViewById(R.id.limitBoundsCb);
        mMapView = v.findViewById(R.id.mapview);

        horizontalCb.setChecked(true);
        verticalCb.setChecked(true);
        limitBoundsCb.setChecked(false);

        horizontalCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMapView.setHorizontalMapRepetitionEnabled(isChecked);
            }
        });

        verticalCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMapView.setVerticalMapRepetitionEnabled(isChecked);
            }
        });

        limitBoundsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMapView.setScrollableAreaLimitDouble(new BoundingBox(
                            mMapView.getTileSystem().getMaxLatitude(), mMapView.getTileSystem().getMaxLongitude(),
                            mMapView.getTileSystem().getMinLatitude(), mMapView.getTileSystem().getMinLongitude()));
                } else {
                    mMapView.setScrollableAreaLimitDouble(null);
                }
            }
        });

        return v;
    }
}

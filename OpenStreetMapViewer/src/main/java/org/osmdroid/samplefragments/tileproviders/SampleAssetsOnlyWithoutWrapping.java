package org.osmdroid.samplefragments.tileproviders;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import org.osmdroid.R;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.drawing.CustomPaintingSurface;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

/**
 * test for showing the map only once without wrapping
 * https://github.com/osmdroid/osmdroid/issues/183
 * Created by Maradox on 11/26/17.
 */
public class SampleAssetsOnlyWithoutWrapping extends BaseSampleFragment {

    CheckBox horizontalCb;
    CheckBox verticalCb;
    CheckBox limitBoundsCb;

    @Override
    public String getSampleTitle() {
        return "Assets Only Without Wrapping";
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

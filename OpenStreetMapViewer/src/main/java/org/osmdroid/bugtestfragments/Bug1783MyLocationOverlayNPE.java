package org.osmdroid.bugtestfragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


/**
 * created on 5/30/2022.
 *
 * @author seadowg
 */
public class Bug1783MyLocationOverlayNPE extends DialogFragment {


    private MyLocationNewOverlay myLocationNewOverlay;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return new MapView(getContext());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        MapView  mapView = (MapView) view;
        myLocationNewOverlay = new MyLocationNewOverlay(mapView);
        mapView.getOverlays().add(myLocationNewOverlay);
    }


    @Override
    public void onPause() {
        super.onPause();
        myLocationNewOverlay.disableFollowLocation();
    }
}

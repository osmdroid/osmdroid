package org.osmdroid.samplefragments;

import android.graphics.Point;
import android.util.DisplayMetrics;

/**
 * Created by alex on 10/10/16.
 */
public class SampleAdvancedNav extends SampleHeadingCompassUp {

    //add animationTo custom screen point
    //IMapController.void animateTo(IGeoPoint geoPoint, Point screenPoint, int animationDuration);
    //MapView public void setMapRotationPoint(Point point) {
    //instead of rotating about the center point of the map while in a driving mode, you can rotate at any point
    @Override
    public String getSampleTitle() {

        return "Advanced Driving Mode";
    }

    public void addOverlays(){
        super.addOverlays();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height=dm.heightPixels;

        mMapView.setMapRotationPoint(new Point(width/2, height * 3/4));
    }

    //insert of mylocaion geting at
}
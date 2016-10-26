package org.osmdroid.views.overlay.simplefastpoint;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is just a wrapper for a List of {@link IGeoPoint} to be used in
 * {@link SimpleFastPointOverlay}.
 * More complex datasets should implement {@link SimpleFastPointOverlay.PointAdapter}
 * Created by Miguel Porto on 26-10-2016.
 */

public class XYPointTheme implements SimpleFastPointOverlay.PointAdapter {
    private final List<IGeoPoint> mPoints;

    public XYPointTheme() {
        mPoints = new ArrayList<>();
    }

    public XYPointTheme(List<IGeoPoint> pPoints) {
        mPoints = pPoints;
    }

    @Override
    public int size() {
        return mPoints.size();
    }

    @Override
    public IGeoPoint get(int i) {
        return mPoints.get(i);
    }

    /**
     * NOTE: this iterator will be called very frequently, avoid complicated code.
     * @return
     */
    @Override
    public Iterator<IGeoPoint> iterator() {
        return mPoints.iterator();
    }

}

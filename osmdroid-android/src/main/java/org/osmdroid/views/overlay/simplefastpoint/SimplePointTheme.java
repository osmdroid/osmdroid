package org.osmdroid.views.overlay.simplefastpoint;

import org.osmdroid.api.IGeoPoint;

import java.util.Iterator;
import java.util.List;

/**
 * This class is just a simple wrapper for a List of {@link IGeoPoint}s to be used in
 * {@link SimpleFastPointOverlay}. Can be used for unlabelled or labelled GeoPoints. Be sure to set
 * the labelled parameter of the constructor to match the kind of points.
 * More complex cases should implement {@link SimpleFastPointOverlay.PointAdapter}, not extend this
 * one.
 * Created by Miguel Porto on 26-10-2016.
 */

public final class SimplePointTheme implements SimpleFastPointOverlay.PointAdapter {
    private final List<IGeoPoint> mPoints;
    private boolean mLabelled;

    public SimplePointTheme(List<IGeoPoint> pPoints, boolean labelled) {
        mPoints = pPoints;
        mLabelled = labelled;
    }

    @Override
    public int size() {
        return mPoints.size();
    }

    @Override
    public IGeoPoint get(int i) {
        return mPoints.get(i);
    }

    @Override
    public boolean isLabelled() {
        return mLabelled;
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

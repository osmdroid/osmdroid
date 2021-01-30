package org.osmdroid.views.overlay.simplefastpoint;

import org.osmdroid.api.IGeoPoint;

import java.util.Iterator;
import java.util.List;

/**
 * This class is just a simple wrapper for a List of {@link IGeoPoint}s to be used in
 * {@link SimpleFastPointOverlay}. Can be used for unlabelled or labelled GeoPoints.
 * Use the simple constructor, or otherwise be sure to set the labelled and styled parameters of the
 * constructor to match the kind of points.
 * More complex cases should implement {@link SimpleFastPointOverlay.PointAdapter}, not extend this
 * one. This is a simple example on how to implement an adapter for any case.
 * Created by Miguel Porto on 26-10-2016.
 */

public final class SimplePointTheme implements SimpleFastPointOverlay.PointAdapter {
    private final List<IGeoPoint> mPoints;
    private boolean mLabelled, mStyled;

    public SimplePointTheme(List<IGeoPoint> pPoints) {
        this(pPoints, pPoints.size() != 0 && pPoints.get(0) instanceof LabelledGeoPoint
                , pPoints.size() != 0 && pPoints.get(0) instanceof StyledLabelledGeoPoint);
    }

    public SimplePointTheme(List<IGeoPoint> pPoints, boolean labelled) {
        this(pPoints, labelled, false);
    }

    public SimplePointTheme(List<IGeoPoint> pPoints, boolean labelled, boolean styled) {
        mPoints = pPoints;
        mLabelled = labelled;
        mStyled = styled;
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

    @Override
    public boolean isStyled() {
        return mStyled;
    }

    /**
     * NOTE: this iterator will be called very frequently, avoid complicated code.
     *
     * @return
     */
    @Override
    public Iterator<IGeoPoint> iterator() {
        return mPoints.iterator();
    }

}

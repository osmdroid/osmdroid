package org.osmdroid.views.overlay;

/**
 * A milestone step is a pixel position where a milestone should be displayed with an orientation
 * Created by Fabrice Fontaine on 20/12/2017.
 * @since 6.0.0
 */
public class MilestoneStep {

    private final long mX;
    private final long mY;
    private final double mOrientation; // in degree

    public MilestoneStep(final long pX, final long pY, final double pOrientation) {
        mX = pX;
        mY = pY;
        mOrientation = pOrientation;
    }

    public long getX() {
        return mX;
    }

    public long getY() {
        return mY;
    }

    public double getOrientation() {
        return mOrientation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + mX + "," + mY + "," + mOrientation;
    }
}

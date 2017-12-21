package org.osmdroid.views.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import org.osmdroid.util.Distance;
import org.osmdroid.util.PointL;
import org.osmdroid.views.Projection;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.ArrayList;

/**
 * Created by Jason Marks on 12/2/2017.
 */

class ArrowsLinearRing extends LinearRing {

    private double mDistance;
    private final ArrayList<MilestoneStep> mMilestones = new ArrayList<>();
    private static final float DEFAULT_ARROW_LENGTH = 20f;
    private static final boolean DEFAULT_INVERT_ARROWS = false;
    private boolean mDrawDirectionalArrows = false;
    private boolean mInvertDirectionalArrows = DEFAULT_INVERT_ARROWS;
    private float mDirectionalArrowLength = DEFAULT_ARROW_LENGTH;

    public ArrowsLinearRing(final Path pPath) {
        super(pPath);
    }

    @Override
    public void lineTo(final long pX, final long pY) {
        if (!mDrawDirectionalArrows) {
            super.lineTo(pX, pY);
        } else {
            if (!getIsNextMove()) {
                PointL mPreviousPoint = new PointL(getLatestPathPoint());
//                addMilestoneInTheMiddle(mPreviousPoint.x, mPreviousPoint.y, pX, pY);
                addMilestoneAsDistance(mPreviousPoint.x, mPreviousPoint.y, pX, pY);
            }
            super.lineTo(pX, pY);
        }
    }

    @Override
    void clearPath() {
        super.clearPath();
        mMilestones.clear();
        mDistance = 0;
    }

    @Override
    PointL buildPathPortion(final Projection pProjection,
                            final boolean pClosePath, final PointL pOffset){
        if (!mMilestones.isEmpty()) {
            mMilestones.clear();
        }
        mDistance = 0;

        return super.buildPathPortion(pProjection, pClosePath, pOffset);
    }

    /**
     * Update the stroke width
     *
     * @param strokeWidth
     */
    public void setStrokeWidth(final float strokeWidth) {
        this.mDirectionalArrowLength = DEFAULT_ARROW_LENGTH + strokeWidth;
    }

    /**
     * A directional arrow is a single arrow drawn in the middle of two points to
     * provide a visual cue for direction of movement between the two points.
     *
     * By default the arrows always point towards the lower index as the list of GeoPoints are
     * processed. The direction the arrows point can be inverted.
     *
     * @param drawDirectionalArrows to enable or disable
     * @param invertDirection invert the direction the arrows are drawn. Use null for default value
     * @param strokeWidth the current stroke width of the paint that describes the object
     */
    void setDrawDirectionalArrows(final boolean drawDirectionalArrows,
                                  final Boolean invertDirection, final float strokeWidth) {
        this.mDrawDirectionalArrows = drawDirectionalArrows;
        // reset defaults if disabling
        if (!drawDirectionalArrows) {
            mInvertDirectionalArrows = DEFAULT_INVERT_ARROWS;
            return;
        }
        setStrokeWidth(strokeWidth);
        if (invertDirection != null) {
            this.mInvertDirectionalArrows = invertDirection;
        }
    }

    /**
     * If enabled, draw the directional arrows
     *
     * @param canvas the canvas to draw on
     */
    void drawDirectionalArrows(final Canvas canvas, final Paint mPaint, final Bitmap pBitmap) {
        if (!mDrawDirectionalArrows) {
            return;
        }
        final Path path;
        final Paint fillPaint;
        if (pBitmap == null) {
            fillPaint = new Paint(mPaint);
            fillPaint.setStyle(Paint.Style.FILL);
            path = new Path();
        } else {
            fillPaint = null;
            path = null;
        }
        for (final MilestoneStep step : mMilestones) {
            canvas.save();
            canvas.rotate((float)step.getOrientation(), step.getX(), step.getY());
            if (pBitmap != null) {
                canvas.drawBitmap(pBitmap, step.getX() - pBitmap.getWidth() / 2, step.getY() - pBitmap.getHeight() / 2, null);
            } else {
                buildArrowPath(step, path);
                canvas.drawPath(path, fillPaint);
            }
            canvas.restore();

            if (mInvertDirectionalArrows) {
                // TODO 0000 too
            }
        }
    }

    private void addMilestoneInTheMiddle(final long x0, final long y0, final long x1, final long y1) {
        // if the points are really close don't draw an arrow
        if (Distance.getSquaredDistanceToPoint(x0, y0, x1, y1) <= mDirectionalArrowLength) { // TODO 000 sqrt?
            return;
        }

        final long centerX = (x0 + x1) / 2;
        final long centerY = (y0 + y1) / 2;
        final double orientation = getOrientation(x0, y0, x1, y1);
        final MilestoneStep step = new MilestoneStep(centerX, centerY, orientation);
        mMilestones.add(step);
    }

    private final static double nbPixels = 100;

    // TODO 0000 test + static
    /**
     * @return the orientation (in degrees) of the slope between point p0 and p1, or 0 if same point
     * @since 6.0.0
     */
    private double getOrientation(final long x0, final long y0, final long x1, final long y1) {
        if (x0 == x1) {
            if (y0 == y1) {
                return 0;
            }
            if (y0 > y1) {
                return -90;
            }
            return 90;
        }
        final double slope = ((double)(y1 - y0)) / (x1 - x0);
        final boolean isBeyondHalfPI = x1 < x0;
        return MathConstants.RAD2DEG * Math.atan(slope) + (isBeyondHalfPI ? 180 : 0);
    }

    private void addMilestoneAsDistance(final long x0, final long y0, final long x1, final long y1) {
        double currentDistance = Math.sqrt(Distance.getSquaredDistanceToPoint(x0, y0, x1, y1));
        if (currentDistance == 0) {
            return;
        }
        final double orientation = getOrientation(x0, y0, x1, y1);
        double x = x0;
        double y = y0;
        while(true) {
            final double latestMilestone = Math.floor(mDistance / nbPixels) * nbPixels;
            final double neededForNext = latestMilestone + nbPixels - mDistance;
            if (currentDistance < neededForNext) {
                mDistance += currentDistance;
                return;
            }
            mDistance += neededForNext;
            currentDistance -= neededForNext;
            x += neededForNext * Math.cos(MathConstants.DEG2RAD * orientation);
            y += neededForNext * Math.sin(MathConstants.DEG2RAD * orientation);
            final MilestoneStep step = new MilestoneStep((long)x, (long)y, orientation);
            mMilestones.add(step);
        }
    }

    private void buildArrowPath(final MilestoneStep pStep, final Path pReuse) {
        final Path path = pReuse != null ? pReuse : new Path();
        path.rewind();
        path.moveTo(pStep.getX() - 10, pStep.getY() - 10);
        path.lineTo(pStep.getX() + 10, pStep.getY());
        path.lineTo(pStep.getX() - 10, pStep.getY() + 10);
        path.close();
    }
}

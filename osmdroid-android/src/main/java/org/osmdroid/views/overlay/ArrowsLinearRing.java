package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import org.osmdroid.util.PointL;
import org.osmdroid.util.SegmentClipper;
import org.osmdroid.views.Projection;

import java.util.ArrayList;

/**
 * Created by Jason Marks on 12/2/2017.
 */

class ArrowsLinearRing extends LinearRing implements SegmentClipper.SegmentClippable {

    private final ArrayList<Path> mDirectionalArrows = new ArrayList<>();
    private static final float DEFAULT_ARROW_LENGTH = 20f;
    private static final boolean DEFAULT_INVERT_ARROWS = false;
    private boolean mDrawDirectionalArrows = false;
    private boolean mInvertDirectionalArrows = DEFAULT_INVERT_ARROWS;
    private float mDirectionalArrowLength = DEFAULT_ARROW_LENGTH;

    public ArrowsLinearRing(final Path pPath) {
        super(pPath);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void lineTo(final long pX, final long pY) {
        if (!mDrawDirectionalArrows) {
            super.lineTo(pX, pY);
        } else {
            if (!getIsNextMove()) {
                PointL mPreviousPoint = new PointL(getLatestPathPoint());
                addDirectionalArrow(pX, pY, mPreviousPoint.x, mPreviousPoint.y);
            }
            super.lineTo(pX, pY);
        }
    }

    @Override
    void clearPath() {
        super.clearPath();
        mDirectionalArrows.clear();
    }

    @Override
    PointL buildPathPortion(final Projection pProjection,
                            final boolean pClosePath, final PointL pOffset){
        if (!mDirectionalArrows.isEmpty()) {
            mDirectionalArrows.clear();
        }

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
    void drawDirectionalArrows(final Canvas canvas, final Paint mPaint) {
        if (mDrawDirectionalArrows) {
            if (mDirectionalArrows != null && mDirectionalArrows.size() > 0) {
                Paint fillPaint = new Paint(mPaint);
                fillPaint.setStyle(Paint.Style.FILL);
                for (Path p : mDirectionalArrows) {
                    canvas.drawPath(p, fillPaint);
                }
            }
        }
    }

    private void addDirectionalArrow(final long x0, final long y0, final long x1, final long y1) {
        PointL screenPoint0 = new PointL(x0, y0);
        PointL screenPoint1 = new PointL(x1, y1);

        // if the points are really close don't draw an arrow
        if (Math.abs(screenPoint0.x - screenPoint1.x) +
                Math.abs(screenPoint0.y - screenPoint1.y) <= mDirectionalArrowLength) {
            return;
        }

        if (mInvertDirectionalArrows) {
            PointL temp = screenPoint0;
            screenPoint0 = screenPoint1;
            screenPoint1 = temp;
        }

        // mid point in projected pixels
        PointL screenPoint3 = new PointL();
        PointL screenPoint4 = new PointL();
        screenPoint3.x = (screenPoint0.x + screenPoint1.x) / 2;
        screenPoint3.y = (screenPoint0.y + screenPoint1.y) / 2;
        double delta_x = screenPoint1.x - screenPoint0.x;
        double delta_y = screenPoint1.y - screenPoint0.y;

        float distance = mDirectionalArrowLength;

        if (delta_x == 0) {
            screenPoint3.x = screenPoint1.x;
            screenPoint4.x = screenPoint1.x;

            if (screenPoint0.y > screenPoint1.y) {
                screenPoint3.y = screenPoint0.y + (int) Math.round(delta_y/2);
                screenPoint4.y = screenPoint3.y - (int) distance;
            } else {
                screenPoint3.y = screenPoint0.y + (int) Math.round(delta_y/2);
                screenPoint4.y = screenPoint3.y + (int) distance;
            }
        } else if (delta_y == 0) {
            screenPoint3.y = screenPoint1.y;
            screenPoint4.y = screenPoint1.y;

            if (screenPoint0.x > screenPoint1.x) {
                screenPoint3.x = screenPoint0.x + (int) Math.round(delta_x/2);
                screenPoint4.x = screenPoint3.x - (int) distance;
            } else {
                screenPoint3.x = screenPoint0.x + (int) Math.round(delta_x/2);
                screenPoint4.x = screenPoint3.x + (int) distance;
            }
        } else {
            double slope = delta_y / delta_x;
			 /*  The formula will calculate a new X,Y coordinate that is some distance away from
				a given coordinate when a slope is known. Distance is the correct way of
				thinking about what the number is in regards to the formula used. It can also
				be thought of as the length of the squares diagonal.
			*/
            double r = Math.sqrt(1 + Math.pow(slope, 2));
            // move pt3 half the distance of the square so pt 5 and 6 will intersect the midpt
            screenPoint3.x = screenPoint3.x + (int) Math.round((distance / 2) / r);
            screenPoint3.y = screenPoint3.y + (int) Math.round((distance / 2) * slope / r);
            //calculate another point on the line that is distance in px away from the mid point
            screenPoint4.x = screenPoint3.x - (int) Math.round((distance / r));
            screenPoint4.y = screenPoint3.y - (int) Math.round((distance * slope / r));
        }
		/* 3rd point in the square
			= ( ( x1 + x3 + y1 - y3 ) / 2 , ( x3 - x1 + y1 + y3 ) / 2 )
		*/
        PointL screenPoint5 = new PointL();
        screenPoint5.x =
                (screenPoint4.x + screenPoint3.x + screenPoint4.y - screenPoint3.y) / 2;
        screenPoint5.y =
                (screenPoint3.x - screenPoint4.x + screenPoint4.y + screenPoint3.y) / 2;
		/* 4th point in the square
			= ( ( x1 + x3 + y3 - y1 ) / 2 , ( x1 - x3 + y1 + y3 ) / 2 )
		*/
        PointL screenPoint6 = new PointL();
        screenPoint6.x =
                (screenPoint4.x + screenPoint3.x + screenPoint3.y - screenPoint4.y) / 2;
        screenPoint6.y =
                (screenPoint4.x - screenPoint3.x + screenPoint3.y + screenPoint4.y) / 2;

        Path directionalArrowPath = new Path();
        if (screenPoint0.x > screenPoint1.x && screenPoint0.y > screenPoint1.y) {
            directionalArrowPath.moveTo(screenPoint5.x, screenPoint5.y);
            directionalArrowPath.lineTo(screenPoint3.x, screenPoint3.y);
            directionalArrowPath.lineTo(screenPoint6.x, screenPoint6.y);
        } else if (screenPoint0.x < screenPoint1.x && screenPoint0.y < screenPoint1.y) {
            directionalArrowPath.moveTo(screenPoint5.x, screenPoint5.y);
            directionalArrowPath.lineTo(screenPoint4.x, screenPoint4.y);
            directionalArrowPath.lineTo(screenPoint6.x, screenPoint6.y);
        } else if (screenPoint0.x < screenPoint1.x && screenPoint0.y > screenPoint1.y) {
            directionalArrowPath.moveTo(screenPoint5.x, screenPoint5.y);
            directionalArrowPath.lineTo(screenPoint4.x, screenPoint4.y);
            directionalArrowPath.lineTo(screenPoint6.x, screenPoint6.y);
        } else {
            directionalArrowPath.moveTo(screenPoint5.x, screenPoint5.y);
            directionalArrowPath.lineTo(screenPoint3.x, screenPoint3.y);
            directionalArrowPath.lineTo(screenPoint6.x, screenPoint6.y);
        }
        directionalArrowPath.close();

        mDirectionalArrows.add(directionalArrowPath);
    }
}

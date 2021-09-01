package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.util.RectL;
import org.osmdroid.util.SpeechBalloonHelper;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * Overlay that display a title in a "speech balloon"
 *
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class SpeechBalloonOverlay extends Overlay {

    private final SpeechBalloonHelper mHelper = new SpeechBalloonHelper();
    private final RectL mRect = new RectL();
    private final PointL mPoint = new PointL();
    private final PointL mIntersection1 = new PointL();
    private final PointL mIntersection2 = new PointL();
    private final Path mPath = new Path();
    private final Rect mTextRect = new Rect();
    private final Point mPixel = new Point();

    private String mTitle;
    private GeoPoint mGeoPoint;
    private Paint mBackground;
    private Paint mForeground;
    private int mMargin;
    private double mRadius;
    private int mOffsetX;
    private int mOffsetY;

    private boolean mDraggable = true;
    private boolean mIsDragged;
    private float mDragStartX;
    private float mDragStartY;
    private float mDragDeltaX;
    private float mDragDeltaY;
    private Paint mDragBackground;
    private Paint mDragForeground;

    // TODO animation in / out
    // TODO paint border
    // TODO rounded corners
    // TODO option: no Geopoint, but a "fixed" pixel instead
    // TODO oriented

    public void setTitle(final String pTitle) {
        mTitle = pTitle;
    }

    public void setGeoPoint(final GeoPoint pGeoPoint) {
        mGeoPoint = pGeoPoint;
    }

    public void setBackground(final Paint pBackground) {
        mBackground = pBackground;
    }

    public void setForeground(final Paint pForeground) {
        mForeground = pForeground;
    }

    public void setDragBackground(final Paint pDragBackground) {
        mDragBackground = pDragBackground;
    }

    public void setDragForeground(final Paint pDragForeground) {
        mDragForeground = pDragForeground;
    }

    public void setMargin(final int pMargin) {
        mMargin = pMargin;
    }

    public void setRadius(final long pRadius) {
        mRadius = pRadius;
    }

    public void setOffset(final int pOffsetX, final int pOffsetY) {
        mOffsetX = pOffsetX;
        mOffsetY = pOffsetY;
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        final Paint background;
        final Paint foreground;
        if (mIsDragged) {
            background = mDragBackground != null ? mDragBackground : mBackground;
            foreground = mDragForeground != null ? mDragForeground : mForeground;
        } else {
            background = mBackground;
            foreground = mForeground;
        }
        if (mGeoPoint == null) {
            return;
        }
        if (mTitle == null || mTitle.trim().length() == 0) {
            return;
        }
        if (foreground == null || background == null) {
            return;
        }
        pProjection.toPixels(mGeoPoint, mPixel);
        final String text = mTitle;
        foreground.getTextBounds(text, 0, text.length(), mTextRect);
        mPoint.set(mPixel.x, mPixel.y);
        mTextRect.offset((int) (mPoint.x + mOffsetX + mDragDeltaX), (int) (mPoint.y + mOffsetY + mDragDeltaY));
        mTextRect.top -= mMargin;
        mTextRect.left -= mMargin;
        mTextRect.right += mMargin;
        mTextRect.bottom += mMargin;
        mRect.set(mTextRect.left, mTextRect.top, mTextRect.right, mTextRect.bottom);
        final int corner = mHelper.compute(mRect, mPoint, mRadius, mIntersection1, mIntersection2);
        pCanvas.drawRect(mTextRect.left, mTextRect.top, mTextRect.right, mTextRect.bottom, background);
        if (corner != SpeechBalloonHelper.CORNER_INSIDE) {
            mPath.reset();
            mPath.moveTo(mPoint.x, mPoint.y);
            mPath.lineTo(mIntersection1.x, mIntersection1.y);
            mPath.lineTo(mIntersection2.x, mIntersection2.y);
            mPath.close();
            pCanvas.drawPath(mPath, background);
        }
        pCanvas.drawText(text, mTextRect.left + mMargin, mTextRect.bottom - mMargin, foreground);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        boolean touched = hitTest(event, mapView);
        if (touched) {
            if (mDraggable) {
                //starts dragging mode:
                mIsDragged = true;
                mDragStartX = event.getX();
                mDragStartY = event.getY();
                mDragDeltaX = 0;
                mDragDeltaY = 0;
                mapView.invalidate();
            }
        }
        return touched;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (mDraggable && mIsDragged) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDragDeltaX = event.getX() - mDragStartX;
                mDragDeltaY = event.getY() - mDragStartY;
                mOffsetX += mDragDeltaX;
                mOffsetY += mDragDeltaY;
                mDragDeltaX = 0;
                mDragDeltaY = 0;
                mIsDragged = false;
                mapView.invalidate();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mDragDeltaX = event.getX() - mDragStartX;
                mDragDeltaY = event.getY() - mDragStartY;
                mapView.invalidate();
                return true;
            }
        }
        return false;
    }

    private boolean hitTest(final MotionEvent event, final MapView mapView) {
        return mRect.contains((int) event.getX(), (int) event.getY());
    }
}

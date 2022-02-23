package org.osmdroid.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import org.osmdroid.library.R;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class CustomZoomButtonsDisplay {

    public enum HorizontalPosition {LEFT, CENTER, RIGHT}

    public enum VerticalPosition {TOP, CENTER, BOTTOM}

    private final MapView mMapView;
    private final Point mUnrotatedPoint = new Point();
    private Bitmap mZoomInBitmapEnabled;
    private Bitmap mZoomOutBitmapEnabled;
    private Bitmap mZoomInBitmapDisabled;
    private Bitmap mZoomOutBitmapDisabled;
    private Paint mAlphaPaint;
    private int mBitmapSize;
    private HorizontalPosition mHorizontalPosition;
    private VerticalPosition mVerticalPosition;
    private boolean mHorizontalOrVertical;
    private float mMargin; // as fraction of the bitmap size
    private float mPadding; // as fraction of the bitmap size
    private float mAdditionalPixelMarginLeft; // additional left margin in pixels
    private float mAdditionalPixelMarginTop; // additional top margin in pixels
    private float mAdditionalPixelMarginRight; // additional right margin in pixels
    private float mAdditionalPixelMarginBottom;    // additional bottom margin in pixels
    private float mPixelMarginLeft; // calculated overall left margin in pixels
    private float mPixelMarginTop; // calculated overall top margin in pixels
    private float mPixelMarginRight; // calculated overall right margin in pixels
    private float mPixelMarginBottom;    // calculated overall bottom margin in pixels

    public CustomZoomButtonsDisplay(final MapView pMapView) {
        mMapView = pMapView;
        // default values
        setPositions(true, HorizontalPosition.CENTER, VerticalPosition.BOTTOM);
        setMarginPadding(.5f, .5f);
    }

    public void setPositions(
            final boolean pHorizontalOrVertical,
            final HorizontalPosition pHorizontalPosition, final VerticalPosition pVerticalPosition) {
        mHorizontalOrVertical = pHorizontalOrVertical;
        mHorizontalPosition = pHorizontalPosition;
        mVerticalPosition = pVerticalPosition;
    }

    /**
     * sets margin and padding as fraction of the bitmap width
     */
    public void setMarginPadding(final float pMargin, final float pPadding) {
        mMargin = pMargin;
        mPadding = pPadding;
        refreshPixelMargins();
    }

    /**
     * sets additional margin in pixels
     *
     * @since 6.1.3
     */
    public void setAdditionalPixelMargins(final float pLeft, final float pTop, final float pRight, final float pBottom) {
        mAdditionalPixelMarginLeft = pLeft;
        mAdditionalPixelMarginTop = pTop;
        mAdditionalPixelMarginRight = pRight;
        mAdditionalPixelMarginBottom = pBottom;
        refreshPixelMargins();
    }

    /**
     * calculate overall margins in pixels
     *
     * @since 6.1.3
     */
    private void refreshPixelMargins() {
        final float bitmapFractionMarginInPixels = mMargin * mBitmapSize;
        mPixelMarginLeft = bitmapFractionMarginInPixels + mAdditionalPixelMarginLeft;
        mPixelMarginTop = bitmapFractionMarginInPixels + mAdditionalPixelMarginTop;
        mPixelMarginRight = bitmapFractionMarginInPixels + mAdditionalPixelMarginRight;
        mPixelMarginBottom = bitmapFractionMarginInPixels + mAdditionalPixelMarginBottom;
    }

    public void setBitmaps(final Bitmap pInEnabled, final Bitmap pInDisabled,
                           final Bitmap pOutEnabled, final Bitmap pOutDisabled) {
        mZoomInBitmapEnabled = pInEnabled;
        mZoomInBitmapDisabled = pInDisabled;
        mZoomOutBitmapEnabled = pOutEnabled;
        mZoomOutBitmapDisabled = pOutDisabled;
        mBitmapSize = mZoomInBitmapEnabled.getWidth();
        refreshPixelMargins();
    }

    protected Bitmap getZoomBitmap(final boolean pInOrOut, final boolean pEnabled) {
        final Bitmap icon = getIcon(pInOrOut);
        mBitmapSize = icon.getWidth();
        refreshPixelMargins();
        final Bitmap bitmap = Bitmap.createBitmap(mBitmapSize, mBitmapSize, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(pEnabled ? Color.WHITE : Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, mBitmapSize - 1, mBitmapSize - 1, backgroundPaint);
        canvas.drawBitmap(icon, 0, 0, null);
        return bitmap;
    }

    protected Bitmap getIcon(final boolean pInOrOut) {
        final int resourceId = pInOrOut ? R.drawable.sharp_add_black_36 : R.drawable.sharp_remove_black_36;
        return ((BitmapDrawable) mMapView.getResources().getDrawable(resourceId)).getBitmap();
    }

    public void draw(final Canvas pCanvas, final float pAlpha01,
                     final boolean pZoomInEnabled, final boolean pZoomOutEnabled) {
        if (pAlpha01 == 0) {
            return;
        }
        final Paint paint;
        if (pAlpha01 == 1) {
            paint = null;
        } else {
            if (mAlphaPaint == null) {
                mAlphaPaint = new Paint();
            }
            mAlphaPaint.setAlpha((int) (pAlpha01 * 255));
            paint = mAlphaPaint;
        }
        pCanvas.drawBitmap(
                getBitmap(true, pZoomInEnabled),
                getTopLeft(true, true),
                getTopLeft(true, false),
                paint);
        pCanvas.drawBitmap(
                getBitmap(false, pZoomOutEnabled),
                getTopLeft(false, true),
                getTopLeft(false, false),
                paint);
    }

    private float getTopLeft(final boolean pInOrOut, final boolean pXOrY) {
        final float topLeft;
        if (pXOrY) {
            topLeft = getFirstLeft(mMapView.getWidth());
            if (!mHorizontalOrVertical) { // vertical: same left
                return topLeft;
            }
            if (!pInOrOut) { // horizontal: zoom out first
                return topLeft;
            }
            return topLeft + mBitmapSize + mPadding * mBitmapSize;
        }
        topLeft = getFirstTop(mMapView.getHeight());
        if (mHorizontalOrVertical) { // horizontal: same top
            return topLeft;
        }
        if (pInOrOut) { // vertical: zoom in first
            return topLeft;
        }
        return topLeft + mBitmapSize + mPadding * mBitmapSize;
    }

    private float getFirstLeft(final int pMapViewWidth) {
        switch (mHorizontalPosition) {
            case LEFT:
                return mPixelMarginLeft;
            case RIGHT:
                return pMapViewWidth - mPixelMarginRight - mBitmapSize
                        - (mHorizontalOrVertical ? mPadding * mBitmapSize + mBitmapSize : 0);
            case CENTER:
                return pMapViewWidth / 2f
                        - (mHorizontalOrVertical ? mPadding * mBitmapSize / 2 + mBitmapSize : mBitmapSize / 2f);
        }
        throw new IllegalArgumentException();
    }

    private float getFirstTop(final int pMapViewHeight) {
        switch (mVerticalPosition) {
            case TOP:
                return mPixelMarginTop;
            case BOTTOM:
                return pMapViewHeight - mPixelMarginBottom - mBitmapSize
                        - (mHorizontalOrVertical ? 0 : mPadding * mBitmapSize + mBitmapSize);
            case CENTER:
                return pMapViewHeight / 2f
                        - (mHorizontalOrVertical ? mBitmapSize / 2f : mPadding * mBitmapSize / 2 + mBitmapSize);
        }
        throw new IllegalArgumentException();
    }

    private Bitmap getBitmap(final boolean pInOrOut, final boolean pEnabled) {
        if (mZoomInBitmapEnabled == null) {
            setBitmaps(
                    getZoomBitmap(true, true),
                    getZoomBitmap(true, false),
                    getZoomBitmap(false, true),
                    getZoomBitmap(false, false)
            );
        }
        if (pInOrOut) {
            return pEnabled ? mZoomInBitmapEnabled : mZoomInBitmapDisabled;
        }
        return pEnabled ? mZoomOutBitmapEnabled : mZoomOutBitmapDisabled;
    }

    @Deprecated
    public boolean isTouchedRotated(final MotionEvent pMotionEvent, final boolean pInOrOut) {
        if (mMapView.getMapOrientation() == 0) {
            mUnrotatedPoint.set((int) pMotionEvent.getX(), (int) pMotionEvent.getY());
        } else {
            mMapView.getProjection().rotateAndScalePoint(
                    (int) pMotionEvent.getX(), (int) pMotionEvent.getY(), mUnrotatedPoint);
        }
        return isTouched(mUnrotatedPoint.x, mUnrotatedPoint.y, pInOrOut);
    }

    /**
     * @since 6.1.3
     */
    public boolean isTouched(final MotionEvent pMotionEvent, final boolean pInOrOut) {
        if (pMotionEvent.getAction() == MotionEvent.ACTION_UP) {
            return isTouched((int) pMotionEvent.getX(), (int) pMotionEvent.getY(), pInOrOut);
        } else {
            return false;
        }
    }

    private boolean isTouched(final int pEventX, final int pEventY, final boolean pInOrOut) {
        return isTouched(pInOrOut, true, pEventX)
                && isTouched(pInOrOut, false, pEventY);
    }

    private boolean isTouched(final boolean pInOrOut, final boolean pXOrY, final float pEvent) {
        final float topLeft = getTopLeft(pInOrOut, pXOrY);
        return pEvent >= topLeft && pEvent <= topLeft + mBitmapSize;
    }
}

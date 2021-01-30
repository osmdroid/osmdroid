package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.Projection;

import java.util.Locale;

/**
 * ScaleDiskOverlay displays a disk of a given radius (distance) around a GeoPoint
 *
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class ScaleDiskOverlay extends Overlay {

    private final Point mPixelCenter = new Point();
    private final Rect mLabelRect = new Rect();

    private final GeoPoint mGeoCenter;
    private final double mMeters;
    private final String mLabel;

    private Paint mCirclePaint1;
    private Paint mCirclePaint2;
    private Paint mTextPaint;

    private Integer mLabelOffsetTop;
    private Integer mLabelOffsetBottom;
    private Integer mLabelOffsetLeft;
    private Integer mLabelOffsetRight;

    private int mDisplaySizeMin;
    private int mDisplaySizeMax;

    public ScaleDiskOverlay(final Context pContext,
                            final GeoPoint pGeoCenter,
                            final int pValue, final GeoConstants.UnitOfMeasure pUnitOfMeasure) {
        mGeoCenter = pGeoCenter;
        mMeters = pValue * pUnitOfMeasure.getConversionFactorToMeters();
        mLabel = ScaleBarOverlay.getScaleString(
                pContext,
                String.format(Locale.getDefault(), "%d", pValue), pUnitOfMeasure);
    }

    /**
     * Circle Paint 1 setter (typically for a disk)
     * Can be null; will be used before Circle Paint 2
     */
    public void setCirclePaint1(final Paint pPaint) {
        mCirclePaint1 = pPaint;
    }

    /**
     * Circle Paint 2 setter (typically for a circle)
     * Can be null; will be used after Circle Paint 1
     */
    public void setCirclePaint2(final Paint pPaint) {
        mCirclePaint2 = pPaint;
    }

    /**
     * Label Paint setter (null means no label will be displayed)
     */
    public void setTextPaint(final Paint pPaint) {
        mTextPaint = pPaint;
    }

    /**
     * Label offset setter for top (null means no label on top)
     */
    public void setLabelOffsetTop(final Integer pValue) {
        mLabelOffsetTop = pValue;
    }

    /**
     * Label offset setter for bottom (null means no label on bottom)
     */
    public void setLabelOffsetBottom(final Integer pValue) {
        mLabelOffsetBottom = pValue;
    }

    /**
     * Label offset setter for left (null means no label on left)
     */
    public void setLabelOffsetLeft(final Integer pValue) {
        mLabelOffsetLeft = pValue;
    }

    /**
     * Label offset setter for right (null means no label on right)
     */
    public void setLabelOffsetRight(final Integer pValue) {
        mLabelOffsetRight = pValue;
    }

    /**
     * Minimum display size setter (<= 0 means no minimum)
     */
    public void setDisplaySizeMin(final int pValue) {
        mDisplaySizeMin = pValue;
    }

    /**
     * Maximum display size setter (<= 0 means no maximum)
     */
    public void setDisplaySizeMax(final int pValue) {
        mDisplaySizeMax = pValue;
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        pProjection.toPixels(mGeoCenter, mPixelCenter);
        final int x = mPixelCenter.x;
        final int y = mPixelCenter.y;
        final int radius = (int) pProjection.metersToPixels(
                (float) mMeters, mGeoCenter.getLatitude(), pProjection.getZoomLevel());
        if (mDisplaySizeMin > 0 && 2 * radius < mDisplaySizeMin) {
            return;
        }
        if (mDisplaySizeMax > 0 && 2 * radius > mDisplaySizeMax) {
            return;
        }
        if (mCirclePaint1 != null) {
            pCanvas.drawCircle(x, y, radius, mCirclePaint1);
        }
        if (mCirclePaint2 != null) {
            pCanvas.drawCircle(x, y, radius, mCirclePaint2);
        }
        if (mTextPaint != null) {
            mTextPaint.getTextBounds(mLabel, 0, mLabel.length(), mLabelRect);
            if (mLabelOffsetTop != null) {
                final int offsetX = getOffsetX();
                final int offsetY = -radius + getOffsetY(mLabelOffsetTop);
                pCanvas.drawText(mLabel, x + offsetX, y + offsetY, mTextPaint);
            }
            if (mLabelOffsetLeft != null) {
                final int offsetX = -radius + getOffsetX(mLabelOffsetLeft);
                final int offsetY = getOffsetY();
                pCanvas.drawText(mLabel, x + offsetX, y + offsetY, mTextPaint);
            }
            if (mLabelOffsetBottom != null) {
                final int offsetX = getOffsetX();
                final int offsetY = radius + getOffsetY(mLabelOffsetBottom);
                pCanvas.drawText(mLabel, x + offsetX, y + offsetY, mTextPaint);
            }
            if (mLabelOffsetRight != null) {
                final int offsetX = radius + getOffsetX(mLabelOffsetRight);
                final int offsetY = getOffsetY();
                pCanvas.drawText(mLabel, x + offsetX, y + offsetY, mTextPaint);
            }
        }
    }

    private int getOffsetX() {
        return -mLabelRect.width() / 2;
    }

    private int getOffsetY() {
        return 0;
    }

    private int getOffsetX(final int pOffsetX) {
        return pOffsetX + (pOffsetX >= 0 ? 0 : -mLabelRect.width());
    }

    private int getOffsetY(final int pOffsetY) {
        return pOffsetY + (pOffsetY >= 0 ? -mLabelRect.top : -mLabelRect.bottom);
    }
}
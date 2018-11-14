package org.osmdroid.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

/**
 * @since 6.1.0
 * @author Fabrice Fontaine
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
	private float mBitmapStrokeWidth;
	private int mBitmapSegmentSize;
	private HorizontalPosition mHorizontalPosition;
	private VerticalPosition mVerticalPosition;
	private boolean mHorizontalOrVertical;
	private int mMargin;
	private int mPadding;

	public CustomZoomButtonsDisplay(final MapView pMapView) {
		mMapView = pMapView;
		// default values
		setPositions(true, HorizontalPosition.CENTER, VerticalPosition.BOTTOM);
		setDrawingSizes(50, 5, 20);
		setMarginPadding(25, 16);
	}

	public void setPositions(
			final boolean pHorizontalOrVertical,
			final HorizontalPosition pHorizontalPosition, final VerticalPosition pVerticalPosition) {
		mHorizontalOrVertical = pHorizontalOrVertical;
		mHorizontalPosition = pHorizontalPosition;
		mVerticalPosition = pVerticalPosition;
	}

	public void setDrawingSizes(final int pSize, final float pStrokeWidth, final int pSegmentSize) {
		mBitmapSize = pSize;
		mBitmapStrokeWidth = pStrokeWidth;
		mBitmapSegmentSize = pSegmentSize;
	}

	public void setMarginPadding(final int pMargin, final int pPadding) {
		mMargin = pMargin;
		mPadding = pPadding;
	}

	public void setBitmaps(final Bitmap pInEnabled, final Bitmap pInDisabled,
						   final Bitmap pOutEnabled, final Bitmap pOutDisabled) {
		mZoomInBitmapEnabled = pInEnabled;
		mZoomInBitmapDisabled = pInDisabled;
		mZoomOutBitmapEnabled = pOutEnabled;
		mZoomOutBitmapDisabled = pOutDisabled;
	}

	protected Bitmap getZoomBitmap(final boolean pInOrOut, final boolean pEnabled) {
		final Bitmap bitmap = Bitmap.createBitmap(mBitmapSize, mBitmapSize, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		final Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(pEnabled ? Color.WHITE : Color.LTGRAY);
		backgroundPaint.setStyle(Paint.Style.FILL);
		final Paint segmentPaint = new Paint();
		segmentPaint.setColor(Color.BLACK);
		segmentPaint.setStyle(Paint.Style.STROKE);
		segmentPaint.setStrokeWidth(mBitmapStrokeWidth);
		final int half = (mBitmapSize - mBitmapSegmentSize) / 2;
		canvas.drawRect(0, 0, mBitmapSize - 1, mBitmapSize - 1, backgroundPaint);
		canvas.drawLine(
				half, mBitmapSize / 2,
				half + mBitmapSegmentSize, mBitmapSize / 2, segmentPaint);
		if (pInOrOut) {
			canvas.drawLine(
					mBitmapSize / 2, half,
					mBitmapSize / 2, half + mBitmapSegmentSize, segmentPaint);
		}
		return bitmap;
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
			mAlphaPaint.setAlpha((int)(pAlpha01 * 255));
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

	private int getTopLeft(final boolean pInOrOut, final boolean pXOrY) {
		final int topLeft;
		if (pXOrY) {
			topLeft = getLeftForZoomIn(mMapView.getWidth());
			if (pInOrOut) {
				return topLeft;
			}
			return mHorizontalOrVertical ? topLeft + mBitmapSize + mPadding : topLeft;
		}
		topLeft = getTopForZoomIn(mMapView.getHeight());
		if (pInOrOut) {
			return topLeft;
		}
		return mHorizontalOrVertical ? topLeft : topLeft + mBitmapSize + mPadding;
	}

	public int getLeftForZoomIn(final int pMapViewWidth) {
		switch(mHorizontalPosition) {
			case LEFT:
				return mMargin;
			case RIGHT:
				return pMapViewWidth - mMargin - mBitmapSize
						- (mHorizontalOrVertical ? mPadding + mBitmapSize : 0);
			case CENTER:
				return pMapViewWidth / 2
						- (mHorizontalOrVertical ? mPadding / 2 + mBitmapSize : mBitmapSize / 2);
		}
		throw new IllegalArgumentException();
	}

	public int getTopForZoomIn(final int pMapViewHeight) {
		switch(mVerticalPosition) {
			case TOP:
				return mMargin;
			case BOTTOM:
				return pMapViewHeight - mMargin - mBitmapSize
						- (mHorizontalOrVertical ? 0 : mPadding + mBitmapSize);
			case CENTER:
				return pMapViewHeight / 2
						- (mHorizontalOrVertical ? mBitmapSize / 2 : mPadding / 2 + mBitmapSize);
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

	public boolean isTouchedRotated(final MotionEvent pMotionEvent, final boolean pInOrOut) {
		if (mMapView.getMapOrientation() == 0) {
			mUnrotatedPoint.set((int) pMotionEvent.getX(), (int) pMotionEvent.getY());
		} else {
			mMapView.getProjection().rotateAndScalePoint(
					(int) pMotionEvent.getX(), (int) pMotionEvent.getY(), mUnrotatedPoint);
		}
		return isTouched(mUnrotatedPoint.x, mUnrotatedPoint.y, pInOrOut);
	}

	private boolean isTouched(final int pEventX, final int pEventY, final boolean pInOrOut) {
		return isTouched(pInOrOut, true, pEventX)
				&& isTouched(pInOrOut, false, pEventY);
	}

	private boolean isTouched(final boolean pInOrOut, final boolean pXOrY, final float pEvent) {
		final int topLeft = getTopLeft(pInOrOut, pXOrY);
		return pEvent >= topLeft && pEvent <= topLeft + mBitmapSize;
	}
}
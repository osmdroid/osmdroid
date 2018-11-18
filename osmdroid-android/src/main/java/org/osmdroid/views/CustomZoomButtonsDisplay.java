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
	private HorizontalPosition mHorizontalPosition;
	private VerticalPosition mVerticalPosition;
	private boolean mHorizontalOrVertical;
	private float mMargin; // as fraction of the bitmap size
	private float mPadding; // as fraction of the bitmap size

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

	public void setMarginPadding(final float pMargin, final float pPadding) {
		mMargin = pMargin;
		mPadding = pPadding;
	}

	public void setBitmaps(final Bitmap pInEnabled, final Bitmap pInDisabled,
						   final Bitmap pOutEnabled, final Bitmap pOutDisabled) {
		mZoomInBitmapEnabled = pInEnabled;
		mZoomInBitmapDisabled = pInDisabled;
		mZoomOutBitmapEnabled = pOutEnabled;
		mZoomOutBitmapDisabled = pOutDisabled;
		mBitmapSize = mZoomInBitmapEnabled.getWidth();
	}

	protected Bitmap getZoomBitmap(final boolean pInOrOut, final boolean pEnabled) {
	    final Bitmap icon = getIcon(pInOrOut);
        mBitmapSize = icon.getWidth();
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
        return ((BitmapDrawable)mMapView.getResources().getDrawable(resourceId)).getBitmap();
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
		switch(mHorizontalPosition) {
			case LEFT:
				return mMargin * mBitmapSize;
			case RIGHT:
				return pMapViewWidth - mMargin * mBitmapSize - mBitmapSize
						- (mHorizontalOrVertical ? mPadding * mBitmapSize + mBitmapSize : 0);
			case CENTER:
				return pMapViewWidth / 2
						- (mHorizontalOrVertical ? mPadding * mBitmapSize / 2 + mBitmapSize : mBitmapSize / 2);
		}
		throw new IllegalArgumentException();
	}

	private float getFirstTop(final int pMapViewHeight) {
		switch(mVerticalPosition) {
			case TOP:
				return mMargin * mBitmapSize;
			case BOTTOM:
				return pMapViewHeight - mMargin * mBitmapSize - mBitmapSize
						- (mHorizontalOrVertical ? 0 : mPadding * mBitmapSize + mBitmapSize);
			case CENTER:
				return pMapViewHeight / 2
						- (mHorizontalOrVertical ? mBitmapSize / 2 : mPadding * mBitmapSize / 2 + mBitmapSize);
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
		final float topLeft = getTopLeft(pInOrOut, pXOrY);
		return pEvent >= topLeft && pEvent <= topLeft + mBitmapSize;
	}
}
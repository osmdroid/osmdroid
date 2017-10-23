package org.osmdroid.views.overlay;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;

import org.osmdroid.library.R;
import org.osmdroid.views.MapView;

/**
 * Replacing the standard ZoomButtonsController
 * @since 6.0.0
 * @author Fabrice Fontaine
 */

public class ZoomButtonsOverlay extends Overlay {

	private static final int[] POSITION_HORIZONTAL_POSSIBLE = new int[] {
			OverlayLayoutParams.LEFT,
			OverlayLayoutParams.RIGHT,
			OverlayLayoutParams.CENTER_HORIZONTAL
	};
	private static final int[] POSITION_VERTICAL_POSSIBLE = new int[] {
			OverlayLayoutParams.TOP,
			OverlayLayoutParams.BOTTOM,
			OverlayLayoutParams.CENTER_VERTICAL
	};
	public static final int POSITION_HORIZONTAL_DEFAULT = OverlayLayoutParams.RIGHT;
	public static final int POSITION_VERTICAL_DEFAULT = OverlayLayoutParams.TOP;

	private final Bitmap mBitmapZoomIn;
	private final Bitmap mBitmapZoomOut;
	private boolean mZoomInEnabled = true;
	private boolean mZoomOutEnabled = true;
	private int mPosition;
	private int mLeft;
	private int mTop;
	private boolean mIsPositioned;

	public ZoomButtonsOverlay(final MapView mapView) {
		super();
		final Resources resources = mapView.getContext().getResources();
		mBitmapZoomIn = BitmapFactory.decodeResource(resources, R.drawable.zoom_in);
		mBitmapZoomOut = BitmapFactory.decodeResource(resources, R.drawable.zoom_out);
	}

	public void setPosition(final int pPosition) {
		mIsPositioned = true;
		mPosition = pPosition;
	}

	public void setLeftTop(final int pLeft, final int pTop) {
		mIsPositioned = false;
		mLeft = pLeft;
		mTop = pTop;
	}

	public void setZoomInEnabled(final boolean pEnabled) {
		mZoomInEnabled = pEnabled;
	}

	public void setZoomOutEnabled(final boolean pEnabled) {
		mZoomOutEnabled = pEnabled;
	}

	@Override
	public void draw(Canvas c, MapView mapView, boolean shadow) {
		if (!isEnabled()) {
			return;
		}
		if (shadow) {
			return;
		}
		c.save();
		c.concat(mapView.getProjection().getInvertedScaleRotateCanvasMatrix());
		if (mZoomOutEnabled) {
			c.drawBitmap(mBitmapZoomOut, getLeft(false, c.getWidth()), getTop(c.getHeight()), null);
		}
		if (mZoomInEnabled) {
			c.drawBitmap(mBitmapZoomIn, getLeft(true, c.getWidth()), getTop(c.getHeight()), null);
		}
		c.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (isEnabled()) {
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			if (mZoomInEnabled &&
					onTouchEvent(
							x, y,
							getLeft(true, mapView.getWidth()), getTop(mapView.getHeight()),
							mBitmapZoomIn.getWidth(), mBitmapZoomIn.getHeight())) {
				mapView.getController().zoomIn();
				return true;
			}
			if (mZoomOutEnabled &&
					onTouchEvent(
							x, y,
							getLeft(false, mapView.getWidth()), getTop(mapView.getHeight()),
							mBitmapZoomOut.getWidth(), mBitmapZoomOut.getHeight())) {
				mapView.getController().zoomOut();
				return true;
			}
		}
		return super.onTouchEvent(event, mapView);
	}

	private int getLeft(final boolean pInOrOut, final int pCanvasWidth) {
		final int bitmapWidth = mBitmapZoomIn.getWidth();
		final int outLeft;
		if (mIsPositioned) {
			outLeft = getPositionLeft(pCanvasWidth, bitmapWidth);
		} else {
			outLeft = mLeft;
		}
		return outLeft + (pInOrOut ? bitmapWidth : 0);
	}

	private int getPositionLeft(final int pCanvasWidth, final int pBitmapWidth) {
		final int position = OverlayLayoutParams.getMaskedValue(
				mPosition, POSITION_HORIZONTAL_DEFAULT, POSITION_HORIZONTAL_POSSIBLE);
		switch(position) {
			case OverlayLayoutParams.LEFT:
				return 0;
			case OverlayLayoutParams.RIGHT:
				return pCanvasWidth - 2 * pBitmapWidth;
			case OverlayLayoutParams.CENTER_HORIZONTAL:
				return (pCanvasWidth - 2 * pBitmapWidth) / 2;
		}
		throw new IllegalArgumentException("Unknown position value: " + mPosition);
	}

	private int getTop(final int pCanvasHeight) {
		final int bitmapHeight = mBitmapZoomIn.getHeight();
		if (mIsPositioned) {
			return getPositionTop(pCanvasHeight, bitmapHeight);
		} else {
			return mTop;
		}
	}

	private int getPositionTop(final int pCanvasHeight, final int pBitmapHeight) {
		final int position = OverlayLayoutParams.getMaskedValue(
				mPosition, POSITION_VERTICAL_DEFAULT, POSITION_VERTICAL_POSSIBLE);
		switch(position) {
			case OverlayLayoutParams.TOP:
				return 0;
			case OverlayLayoutParams.BOTTOM:
				return pCanvasHeight - pBitmapHeight;
			case OverlayLayoutParams.CENTER_VERTICAL:
				return (pCanvasHeight - pBitmapHeight) / 2;
		}
		throw new IllegalArgumentException("Unknown position value: " + mPosition);
	}

	private static boolean onTouchEvent(
			final int pX, final int pY,
			final int pLeft, final int pTop, final int pWidth, final int pHeight) {
		return pX >= pLeft && pX <= pLeft + pWidth && pY >= pTop && pY <= pTop + pHeight;
	}
}

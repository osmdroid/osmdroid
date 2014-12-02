package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.os.Build;

/**
 * This will allow an {@link Overlay} that is not HW acceleration compatible to work in a HW
 * accelerated MapView. It will create a screen-sized backing Bitmap for the Overlay to draw to and
 * then will draw the Bitmap to the HW accelerated canvas. Due to the extra work, it does not draw
 * the shadow layer. If the Canvas passed into the Overlay is not HW accelerated or if
 * {@link #isUsingBackingBitmap()} returns false then it draws normally (including the shadow layer)
 * without the backing Bitmap. <br>
 * <br>
 * TODO:
 * <ol>
 * <li>Implement a flag to determine if the drawing has actually changed. If not, then reuse the
 * last frame's backing bitmap. This will prevent having to re-upload the bitmap texture to GPU.</li>
 * </ol>
 */
public abstract class NonAcceleratedOverlay extends Overlay {
	private static final Logger logger = LoggerFactory.getLogger(NonAcceleratedOverlay.class);

	private Bitmap mBackingBitmap;
	private Canvas mBackingCanvas;
	private final Matrix mBackingMatrix = new Matrix();
	private final Matrix mCanvasIdentityMatrix = new Matrix();

	/**
	 * A delegate for {@link #draw(Canvas, MapView, boolean)}.
	 */
	protected abstract void onDraw(Canvas c, MapView osmv, boolean shadow);

	public NonAcceleratedOverlay(Context ctx) {
		super(ctx);
	}

	public NonAcceleratedOverlay(ResourceProxy pResourceProxy) {
		super(pResourceProxy);
	}

	/**
	 * Override if you really want access to the original (possibly) accelerated canvas.
	 */
	protected void onDraw(Canvas c, Canvas acceleratedCanvas, MapView osmv, boolean shadow) {
		onDraw(c, osmv, shadow);
	}

	/**
	 * Allow forcing this overlay to skip drawing using backing Bitmap by returning false.
	 */
	public boolean isUsingBackingBitmap() {
		return true;
	}

	@Override
	public void onDetach(MapView mapView) {
		mBackingBitmap = null;
		mBackingCanvas = null;
		super.onDetach(mapView);
	}

	@Override
	protected final void draw(Canvas c, MapView osmv, boolean shadow) {
		// First check to see if we want to use the backing bitmap
		final boolean atLeastHoneycomb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
		if (isUsingBackingBitmap() && atLeastHoneycomb && c.isHardwareAccelerated()) {
			// Drawing a shadow layer would require a second backing Bitmap due to the way HW
			// accelerated drawBitmap works. One could extend this Overlay to implement that if
			// needed.
			if (shadow)
				return;

			// If we don't have any drawing area, then don't draw
			if (c.getWidth() == 0 || c.getHeight() == 0)
				return;

			if (mBackingBitmap == null || mBackingBitmap.getWidth() != c.getWidth()
					|| mBackingBitmap.getHeight() != c.getHeight()) {
				mBackingBitmap = null;
				mBackingCanvas = null;
				try {
					mBackingBitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(),
							Config.ARGB_8888);
				} catch (OutOfMemoryError e) {
					logger.error("OutOfMemoryError creating backing bitmap in NonAcceleratedOverlay.");
					System.gc();
					return;
				}

				mBackingCanvas = new Canvas(mBackingBitmap);
			}

			mBackingCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			c.getMatrix(mBackingMatrix);
			mBackingCanvas.setMatrix(mBackingMatrix);
			onDraw(mBackingCanvas, c, osmv, shadow);
			c.save();
			c.getMatrix(mCanvasIdentityMatrix);
			mCanvasIdentityMatrix.invert(mCanvasIdentityMatrix);
			c.concat(mCanvasIdentityMatrix);
			c.drawBitmap(mBackingBitmap, 0, 0, null);
			c.restore();
		} else
			onDraw(c, c, osmv, shadow);
	}
}

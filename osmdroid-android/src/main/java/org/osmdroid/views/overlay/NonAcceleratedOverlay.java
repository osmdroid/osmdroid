package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;

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
 * then will draw the Bitmap to the HW accelerated canvas. If the Canvas passed into the Overlay is
 * not HW accelerated, then it draws normally. Note that drawing a shadow layer does not work. <br/>
 * <br/>
 * TODO:
 * <ol>
 * <li>Implement a flag to determine if the drawing has actually changed. If not, then reuse the
 * last frame's backing bitmap. This will prevent having to re-upload the bitmap texture to GPU.</li>
 * </ol>
 */
public abstract class NonAcceleratedOverlay extends Overlay {

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

	@Override
	protected final void draw(Canvas c, MapView osmv, boolean shadow) {
		// Drawing a shadow layer would require a second backing Bitmap due to the way HW
		// accelerated drawBitmap works. One could extend this Overlay to implement that if needed.
		if (shadow)
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && c.isHardwareAccelerated()) {
			if (mBackingBitmap == null || mBackingBitmap.getWidth() != c.getWidth()
					|| mBackingBitmap.getHeight() != c.getHeight()) {
				mBackingBitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(), Config.ARGB_8888);
				mBackingCanvas = new Canvas(mBackingBitmap);
			}

			mBackingCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			c.getMatrix(mBackingMatrix);
			mBackingCanvas.setMatrix(mBackingMatrix);
			onDraw(mBackingCanvas, osmv, shadow);
			c.save();
			c.getMatrix(mCanvasIdentityMatrix);
			mCanvasIdentityMatrix.invert(mCanvasIdentityMatrix);
			c.concat(mCanvasIdentityMatrix);
			c.drawBitmap(mBackingBitmap, 0, 0, null);
			c.restore();
		} else
			onDraw(c, osmv, shadow);
	}
}

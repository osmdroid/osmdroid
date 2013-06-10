package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafeTranslatedCanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;

/**
 * An overlay class that uses the safe drawing canvas to draw itself and can be zoomed in to high
 * levels without drawing issues.
 * 
 * @see {@link ISafeCanvas}
 */
public abstract class SafeDrawOverlay extends Overlay {

	private static final SafeTranslatedCanvas sSafeCanvas = new SafeTranslatedCanvas();
	private static final Matrix sMatrix = new Matrix();
	private static final float[] sMatrixValues = new float[9];
	private boolean mUseSafeCanvas = true;

	protected abstract void drawSafe(final ISafeCanvas c, final MapView osmv, final boolean shadow);

	public SafeDrawOverlay(Context ctx) {
		super(ctx);
	}

	public SafeDrawOverlay(ResourceProxy pResourceProxy) {
		super(pResourceProxy);
	}

	protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {

		sSafeCanvas.setCanvas(c);

		if (this.isUsingSafeCanvas()) {

			// Find the screen offset
			Rect screenRect = osmv.getProjection().getScreenRect();
			sSafeCanvas.xOffset = -screenRect.left;
			sSafeCanvas.yOffset = -screenRect.top;

			// Save the canvas state
			c.save();

			// Since the translate calls still take a float, there can be rounding errors
			// Let's calculate the error, and adjust for it.
			final int floatErrorX = screenRect.left - (int) (float) screenRect.left;
			final int floatErrorY = screenRect.top - (int) (float) screenRect.top;

			// Translate the coordinates
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				final float scaleX = osmv.getScaleX();
				final float scaleY = osmv.getScaleY();
				c.translate(screenRect.left * scaleX, screenRect.top * scaleY);
				c.translate(floatErrorX, floatErrorY);
			} else {
				c.getMatrix(sMatrix);
				sMatrix.preTranslate(screenRect.left, screenRect.top);
				sMatrix.preTranslate(floatErrorX, floatErrorY);
				c.setMatrix(sMatrix);
			}
		} else {
			sSafeCanvas.xOffset = 0;
			sSafeCanvas.yOffset = 0;
		}
		this.drawSafe(sSafeCanvas, osmv, shadow);

		if (this.isUsingSafeCanvas()) {
			c.restore();
		}
	}

	public boolean isUsingSafeCanvas() {
		return mUseSafeCanvas;
	}

	public void setUseSafeCanvas(boolean useSafeCanvas) {
		mUseSafeCanvas = useSafeCanvas;
	}
}

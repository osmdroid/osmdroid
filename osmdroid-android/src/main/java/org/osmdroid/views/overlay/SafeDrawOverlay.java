package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafeTranslatedCanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

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

			// Get the matrix values
			sMatrix.set(osmv.getMatrix());
			sMatrix.getValues(sMatrixValues);

			// If we're rotating, then reverse the rotation
			// This gets us proper MSCALE values in the matrix.
			final double angrad = Math.atan2(sMatrixValues[Matrix.MSKEW_Y],
					sMatrixValues[Matrix.MSCALE_X]);
			sMatrix.preRotate((float) -Math.toDegrees(angrad), screenRect.centerX(),
					screenRect.centerY());

			// Get the new matrix values to find the scaling factor
			sMatrix.getValues(sMatrixValues);

			// Shift the canvas to remove scroll, while accounting for scaling values
			c.translate(screenRect.left * sMatrixValues[Matrix.MSCALE_X], screenRect.top
					* sMatrixValues[Matrix.MSCALE_Y]);

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

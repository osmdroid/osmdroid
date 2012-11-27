package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafeTranslatedCanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

public abstract class SafeDrawOverlay extends Overlay {

	private static final SafeTranslatedCanvas sSafeCanvas = new SafeTranslatedCanvas();
	private static final Matrix sMatrix = new Matrix();
	private static final float[] sMatrixValues = new float[9];

	protected abstract void drawSafe(final ISafeCanvas c, final MapView osmv, final boolean shadow);

	public SafeDrawOverlay(Context ctx) {
		super(ctx);
	}

	public SafeDrawOverlay(ResourceProxy pResourceProxy) {
		super(pResourceProxy);
	}

	protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {

		sSafeCanvas.setCanvas(c);

		c.getMatrix().getValues(sMatrixValues);

		// Find the screen offset
		Rect screenRect = osmv.getProjection().getScreenRect();
		sSafeCanvas.xOffset = -screenRect.left;
		sSafeCanvas.yOffset = -screenRect.top;

		// Save the canvas state
		c.save();

		// If we're scaling, then we need to adjust
		int xScalingOffset = (screenRect.width() / 2 - (int) (screenRect.width()
				* sMatrixValues[Matrix.MSCALE_X] / 2));
		int yScalingOffset = (screenRect.height() / 2 - (int) (screenRect.height()
				* sMatrixValues[Matrix.MSCALE_Y] / 2));

		// Change the translation values for the matrix
		sMatrixValues[Matrix.MTRANS_X] = 0 + xScalingOffset;
		sMatrixValues[Matrix.MTRANS_Y] = (c.getHeight() - osmv.getHeight()) + yScalingOffset;

		// Install the new matrix
		sMatrix.setValues(sMatrixValues);
		c.setMatrix(sMatrix);

		this.drawSafe(sSafeCanvas, osmv, shadow);

		c.restore();
	}
}

package org.osmdroid.views.safecanvas;

import org.osmdroid.views.overlay.Overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;

/**
 * The SafeBitmapShader class is designed to work in conjunction with {@link SafeTranslatedCanvas}
 * to work around various Android issues with large canvases. For the two classes to work together,
 * call {@link #onDrawCycleStart} at the start of the {@link Overlay#drawSafe} method of your
 * {@link Overlay}. This will set the adjustment needed to draw your BitmapShader safely on the
 * canvas without any drawing distortion at high zoom levels and without any scrolling issues.
 * 
 * @see {@link ISafeCanvas}
 * 
 * @author Marc Kurtz
 * 
 */
public class SafeBitmapShader extends BitmapShader {

	private final Matrix mMatrix = new Matrix();
	
	public SafeBitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) {
		super(bitmap, tileX, tileY);
	}

	/**
	 * This method <b>must</b> be called at the start of the {@link Overlay#drawSafe} draw cycle
	 * method. This will adjust the BitmapShader to the current state of the {@link ISafeCanvas}
	 * passed to it.
	 */
	public void onDrawCycleStart(ISafeCanvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		mMatrix.setTranslate(canvas.getXOffset() % width, canvas.getYOffset() % height);
		this.setLocalMatrix(mMatrix);
	}
}

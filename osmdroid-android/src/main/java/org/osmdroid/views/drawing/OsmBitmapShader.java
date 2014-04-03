package org.osmdroid.views.drawing;

import org.osmdroid.views.MapView;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;

public class OsmBitmapShader extends BitmapShader {
	private final Matrix mMatrix = new Matrix();
	private int mBitmapWidth;
	private int mBitmapHeight;

	public OsmBitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) {
		super(bitmap, tileX, tileY);
		mBitmapWidth = bitmap.getWidth();
		mBitmapHeight = bitmap.getHeight();
	}

	public void onDrawCycle(MapView mapView) {
		mMatrix.setTranslate(-mapView.getScrollX() % mBitmapWidth, -mapView.getScrollY()
				% mBitmapHeight);
		setLocalMatrix(mMatrix);
	}
}

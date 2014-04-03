package org.osmdroid.views.drawing;

import org.osmdroid.views.Projection;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Point;

public class OsmBitmapShader extends BitmapShader {
	private static final Point sPoint = new Point();

	private final Matrix mMatrix = new Matrix();
	private int mBitmapWidth;
	private int mBitmapHeight;

	public OsmBitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) {
		super(bitmap, tileX, tileY);
		mBitmapWidth = bitmap.getWidth();
		mBitmapHeight = bitmap.getHeight();
	}

	public void onDrawCycle(Projection projection) {
		projection.toMercatorPixels(0, 0, sPoint);
		mMatrix.setTranslate(-sPoint.x % mBitmapWidth, -sPoint.y % mBitmapHeight);
		setLocalMatrix(mMatrix);
	}
}

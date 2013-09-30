package org.osmdroid.tileprovider;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

public class BitmapPool {
	final LinkedList<Bitmap> mPool = new LinkedList<Bitmap>();

	private static BitmapPool sInstance;

	public static BitmapPool getInstance() {
		if (sInstance == null)
			sInstance = new BitmapPool();

		return sInstance;
	}

	public void returnDrawableToPool(ReusableBitmapDrawable drawable) {
		Bitmap b = drawable.tryRecycle();
		if (b != null && b.isMutable())
			synchronized (mPool) {
				mPool.addLast(b);
			}
	}

	public void applyReusableOptions(BitmapFactory.Options bitmapOptions) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			Bitmap pooledBitmap = obtainBitmapFromPool();
			bitmapOptions.inBitmap = pooledBitmap;
			bitmapOptions.inSampleSize = 1;
			bitmapOptions.inMutable = true;
		}
	}

	public Bitmap obtainBitmapFromPool() {
		final Bitmap b;
		synchronized (mPool) {
			if (mPool.size() == 0)
				return null;
			else
				b = mPool.removeFirst();
		}

		return b;
	}
}

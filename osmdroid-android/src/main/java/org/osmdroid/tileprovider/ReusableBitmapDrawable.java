package org.osmdroid.tileprovider;

import android.graphics.Bitmap;

/**
 * A {@link ExpirableBitmapDrawable} class that allows keeping track of usage references. This
 * facilitates the ability to reuse the underlying Bitmaps if no references are active. To safely
 * use the Drawable first call {@link #beginUsingDrawable()} and then check {@link #isBitmapValid()}
 * to ensure that the Drawable is still valid. When done using the Drawable you must call
 * {@link #finishUsingDrawable()} to release the reference and allow the Bitmap to be reused later.
 * 
 * @author Marc Kurtz
 * 
 */
public class ReusableBitmapDrawable extends ExpirableBitmapDrawable {

	private boolean mBitmapRecycled = false;
	private int mUsageRefCount = 0;

	public ReusableBitmapDrawable(Bitmap pBitmap) {
		super(pBitmap);
	}

	public void beginUsingDrawable() {
		synchronized (this) {
			mUsageRefCount++;
		}
	}

	public void finishUsingDrawable() {
		synchronized (this) {
			mUsageRefCount--;
			if (mUsageRefCount < 0)
				throw new IllegalStateException("Unbalanced endUsingDrawable() called.");
		}
	}

	public Bitmap tryRecycle() {
		synchronized (this) {
			if (mUsageRefCount == 0) {
				mBitmapRecycled = true;
				return getBitmap();
			}
		}
		return null;
	}

	public boolean isBitmapValid() {
		synchronized (this) {
			return !mBitmapRecycled;
		}
	}
}

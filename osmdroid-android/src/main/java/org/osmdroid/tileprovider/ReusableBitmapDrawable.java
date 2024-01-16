package org.osmdroid.tileprovider;

import android.content.res.Resources;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * A {@link ExpirableBitmapDrawable} class that allows keeping track of usage references. This
 * facilitates the ability to reuse the underlying Bitmaps if no references are active. To safely
 * use the Drawable first call {@link #beginUsingDrawable()} and then check {@link #isBitmapValid()}
 * to ensure that the Drawable is still valid. When done using the Drawable you must call
 * {@link #finishUsingDrawable()} to release the reference and allow the Bitmap to be reused later.
 *
 * @author Marc Kurtz
 */
public class ReusableBitmapDrawable extends ExpirableBitmapDrawable {

    private boolean mBitmapRecycled = false;
    private int mUsageRefCount = 0;

    /**
     * @deprecated This method does't take in count Screen Density, so try to use instead {@link #ReusableBitmapDrawable(Resources, Bitmap)} if you have {@link android.content.Context} or {@link Resources} available
     */
    public ReusableBitmapDrawable(Bitmap pBitmap) {
        super(pBitmap);
    }
    public ReusableBitmapDrawable(@NonNull final Resources res, @NonNull final Bitmap pBitmap) {
        super(res, pBitmap);
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

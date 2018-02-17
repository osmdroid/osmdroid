package org.osmdroid.tileprovider;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.osmdroid.tileprovider.modules.ConfigurablePriorityThreadFactory;

public class BitmapPool {

	private final LinkedList<Bitmap> mPool = new LinkedList<>();
	private final ExecutorService mExecutor = Executors.newFixedThreadPool(1,
			new ConfigurablePriorityThreadFactory(Thread.MIN_PRIORITY, getClass().getName()));

	//singleton: begin
	private BitmapPool() {}

	private static final BitmapPool sInstance = new BitmapPool();

	public static BitmapPool getInstance() {
		return sInstance;
	}
	//singleton: end

	public void returnDrawableToPool(ReusableBitmapDrawable drawable) {
		Bitmap b = drawable.tryRecycle();
		if (b != null && b.isMutable())
			synchronized (mPool) {
				mPool.addLast(b);
			}
	}

	public void applyReusableOptions(final BitmapFactory.Options aBitmapOptions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			aBitmapOptions.inBitmap = obtainBitmapFromPool();
			aBitmapOptions.inSampleSize = 1;
			aBitmapOptions.inMutable = true;
		}
	}

	public Bitmap obtainBitmapFromPool() {
		synchronized (mPool) {
			if (mPool.isEmpty()) {
				return null;
			} else {
				final Bitmap bitmap = mPool.removeFirst();
				if (bitmap.isRecycled()) {
					return obtainBitmapFromPool(); // recurse
				} else {
					return bitmap;
				}
			}
		}
	}

	public Bitmap obtainSizedBitmapFromPool(final int aWidth, final int aHeight) {
		synchronized (mPool) {
			if (mPool.isEmpty()) {
				return null;
			} else {
				for (final Bitmap bitmap : mPool) {
					if (bitmap.isRecycled()) {
						mPool.remove(bitmap);
						return obtainSizedBitmapFromPool(aWidth, aHeight); // recurse to prevent ConcurrentModificationException
					} else if (bitmap.getWidth() == aWidth && bitmap.getHeight() == aHeight) {
						mPool.remove(bitmap);
						return bitmap;
					}
				}
			}
		}

		return null;
	}

	public void clearBitmapPool() {
		synchronized (sInstance.mPool) {
			while (!sInstance.mPool.isEmpty()) {
				Bitmap bitmap = sInstance.mPool.remove();
				bitmap.recycle();
			}
		}
	}

	/**
	 * @since 6.0.0
	 * The same code was duplicated in many places: now there's a unique entry point and it's async
	 */
	public void asyncRecycle(final Drawable pDrawable) {
		if (pDrawable == null) {
			return;
		}
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				syncRecycle(pDrawable);
			}
		});
	}

	/**
	 * @since 6.0.0
	 */
	private void syncRecycle(final Drawable pDrawable) {
		if (pDrawable == null) {
			return;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			if (pDrawable instanceof BitmapDrawable) {
				final Bitmap bitmap = ((BitmapDrawable) pDrawable).getBitmap();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
		if (pDrawable instanceof ReusableBitmapDrawable) {
			returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
		}
	}
}

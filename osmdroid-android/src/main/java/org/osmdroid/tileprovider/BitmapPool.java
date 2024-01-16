package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.modules.ConfigurablePriorityThreadFactory;
import org.osmdroid.util.ReusablePoolDynamic;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitmapPool {

    private final LinkedList<Bitmap> mPool = new LinkedList<>();
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1,
            new ConfigurablePriorityThreadFactory(Thread.MIN_PRIORITY, getClass().getName()));
    private final ReusablePoolDynamic<Drawable,LocalRunnable> mLocalRunnables = new ReusablePoolDynamic<>(new ReusablePoolDynamic.ReusableIndexCallback<>() {
        @Override public ReusablePoolDynamic.ReusableItemSetInterface<Drawable> newInstance() { return new LocalRunnable(); }
    }, 16);

    private final class LocalRunnable implements Runnable, ReusablePoolDynamic.ReusableItemSetInterface<Drawable> {
        private Drawable mDrawable;
        @Override
        public void run() {
            syncRecycle(this.mDrawable);
            mLocalRunnables.setItemElegibleToBeFreed(this, false);
        }
        @Nullable
        @Override
        public Drawable getKey() {
            return this.mDrawable;
        }
        @Override
        public void set(@NonNull final Drawable key) {
            this.mDrawable = key;
        }
        @Override
        public void reset() {
            this.mDrawable = null;
        }
        @Override
        public void freeMemory() {
            if (this.mDrawable != null) {
                if (mDrawable instanceof BitmapDrawable) {
                    ((BitmapDrawable)mDrawable).getBitmap().recycle();
                }
            }
        }
    }

    //singleton: begin
    private BitmapPool() {
    }

    private static final BitmapPool sInstance = new BitmapPool();

    public static BitmapPool getInstance() {
        return sInstance;
    }
    //singleton: end

    public void returnDrawableToPool(ReusableBitmapDrawable drawable) {
        Bitmap b = drawable.tryRecycle();
        if (b != null && !b.isRecycled() && b.isMutable() && b.getConfig() != null) {
            synchronized (mPool) {
                mPool.addLast(b);
            }
        } else if (b != null) {
            Log.d(IMapView.LOGTAG, "Rejected bitmap from being added to BitmapPool.");
        }
    }

    /**
     * @deprecated As of 6.0.2, use
     * {@link #applyReusableOptions(BitmapFactory.Options, int, int)} instead.
     */
    @Deprecated
    public void applyReusableOptions(final BitmapFactory.Options aBitmapOptions) {
        // We can not guarantee a bitmap can be reused without knowing the dimensions, so always
        // return null in inBitmap
        aBitmapOptions.inBitmap = null;
        aBitmapOptions.inSampleSize = 1;
        aBitmapOptions.inMutable = true;
    }

    public void applyReusableOptions(final BitmapFactory.Options aBitmapOptions, final int width, final int height) {
        // This could be optimized for KK and up, as from there on the only requirement is that
        // the reused bitmap's allocatedbytes are >= the size of new one. Since the pool is
        // almost only used for tiles of the same dimensions, the gains will probably be small.
        aBitmapOptions.inBitmap = obtainSizedBitmapFromPool(width, height);
        aBitmapOptions.inSampleSize = 1;
        aBitmapOptions.inMutable = true;
    }

    /**
     * @deprecated As of 6.0.2, use
     * {@link #obtainSizedBitmapFromPool(int, int)} instead.
     */
    @Deprecated
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
    public void asyncRecycle(@Nullable final Drawable pDrawable) {
        if (pDrawable == null) {
            return;
        }
        mExecutor.execute(mLocalRunnables.getFreeItemAndSet(pDrawable));
    }

    /**
     * @since 6.0.0
     */
    private void syncRecycle(final Drawable pDrawable) {
        if (pDrawable == null) {
            return;
        }
        if (pDrawable instanceof ReusableBitmapDrawable) {
            returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
        }
    }
}

package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.test.AndroidTestCase;


public class BitmapPoolTest extends AndroidTestCase {

    private BitmapPool bitmapPool;

    private Bitmap bitmap;
    private Bitmap differentSizeBitmap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        bitmapPool = BitmapPool.getInstance();

        bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        differentSizeBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        bitmapPool.clearBitmapPool();
    }

    public void testThatBitmapIsReusedForSameSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bitmapPool.clearBitmapPool();
            bitmapPool.returnDrawableToPool(new ReusableBitmapDrawable(bitmap));

            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmapPool.applyReusableOptions(options, bitmap.getWidth(), bitmap.getHeight());

            Bitmap testBitmap = options.inBitmap;
            assertNotNull(testBitmap);
        }
    }

    public void testThatBitmapIsNotReusedForDifferentSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bitmapPool.clearBitmapPool();
            bitmapPool.returnDrawableToPool(new ReusableBitmapDrawable(bitmap));

            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmapPool.applyReusableOptions(
                    options, differentSizeBitmap.getWidth(), differentSizeBitmap.getHeight());

            Bitmap testBitmap = options.inBitmap;
            assertNull(testBitmap);
        }
    }

    public void testThatBitmapPoolIsCleared() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bitmapPool.clearBitmapPool();
            bitmapPool.returnDrawableToPool(new ReusableBitmapDrawable(bitmap));

            bitmapPool.clearBitmapPool();

            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmapPool.applyReusableOptions(options, bitmap.getWidth(), bitmap.getHeight());

            Bitmap testBitmap = options.inBitmap;
            assertNull(testBitmap);
        }
    }
}

package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(AndroidJUnit4.class)
public class BitmapPoolTest {

    private BitmapPool bitmapPool;

    private Bitmap bitmap;
    private Bitmap differentSizeBitmap;

    @Before
    public void setUp() {
        bitmapPool = BitmapPool.getInstance();

        bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        differentSizeBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
    }

    @After
    public void tearDown() {
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

    @Test
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

    @Test
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

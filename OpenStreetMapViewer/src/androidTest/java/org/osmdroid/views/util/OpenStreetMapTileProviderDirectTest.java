/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.views.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.MapTileIndex;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Neil Boyd
 */

public class OpenStreetMapTileProviderDirectTest  {

    @Rule
    public ActivityTestRule<StarterMapActivity> activityRule =
            new ActivityTestRule<>(StarterMapActivity.class);

    private MapTileProviderBasic mProvider;

    @Before
    public void setUp() throws Exception {

        mProvider = new MapTileProviderBasic(activityRule.getActivity());

    }

    @After
    public void tearDown() throws Exception {
        mProvider.detach();
    }

    @Test
    public void test_getMapTile_not_found() {
        final long tile = MapTileIndex.getTileIndex(29, 0, 0);

        final Drawable drawable = mProvider.getMapTile(tile);

        assertNull("Expect tile to be null", drawable);
    }

    @Test
    public void test_getMapTile_found() throws RemoteException, BitmapTileSourceBase.LowMemoryException, java.io.IOException {
        final long tile = MapTileIndex.getTileIndex(2, 3, 3);
        if (Build.VERSION.SDK_INT >= 23)
            return;

        // create a bitmap, draw something on it, write it to a file and put it in the cache
        String path = activityRule.getActivity().getFilesDir().getAbsolutePath() + File.separator + "osmdroid" + File.separator;

        File temp = new File(path);
        if (!temp.exists())
            temp.mkdirs();
        Configuration.getInstance().setOsmdroidTileCache(temp);
        ;
        path = path + "OpenStreetMapTileProviderTest.png";
        File f = new File(path);
        if (f.exists())
            f.delete();
        final Bitmap bitmap1 = Bitmap.createBitmap(
                TileSourceFactory.MAPNIK.getTileSizePixels(),
                TileSourceFactory.MAPNIK.getTileSizePixels(),
                Config.ARGB_8888);
        bitmap1.eraseColor(Color.YELLOW);
        final Canvas canvas = new Canvas(bitmap1);

        canvas.drawText("test", 10, 20, new Paint());
        try {
            f.createNewFile();
            final FileOutputStream fos = new FileOutputStream(path);
            bitmap1.compress(CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("unable to write temp tile " + ex);
        }
        final MapTileRequestState state = new MapTileRequestState(tile,
                new ArrayList<MapTileModuleProviderBase>(), mProvider);
        mProvider.mapTileRequestCompleted(state, TileSourceFactory.MAPNIK.getDrawable(path));

        // do the test
        final Drawable drawable = mProvider.getMapTile(tile);
        if (f.exists())
            f.delete();
        assertNotNull("Expect tile to be not null from path " + path, drawable);
        assertTrue("Expect instance of BitmapDrawable", drawable instanceof BitmapDrawable);
        final Bitmap bitmap2 = ((BitmapDrawable) drawable).getBitmap();
        assertNotNull("Expect tile to be not null", bitmap2);

        // compare a few things to see if it's the same bitmap
        // commented out due to a number of intermitent failures on API8
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            assertEquals("Compare config", bitmap1.getConfig(), bitmap2.getConfig());
        }
        assertEquals("Compare width", bitmap1.getWidth(), bitmap2.getWidth());
        assertEquals("Compare height", bitmap1.getHeight(), bitmap2.getHeight());

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            // compare the total thing
            final ByteBuffer bb1 = ByteBuffer.allocate(bitmap1.getWidth() * bitmap1.getHeight() * 4);
            bitmap1.copyPixelsToBuffer(bb1);
            final ByteBuffer bb2 = ByteBuffer.allocate(bitmap2.getWidth() * bitmap2.getHeight() * 4);
            bitmap2.copyPixelsToBuffer(bb2);
            assertEquals("Compare pixels", bb1, bb2);
        }
    }
}

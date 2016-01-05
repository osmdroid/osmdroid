package org.osmdroid.views.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.test.AndroidTestCase;

/**
 * @author Neil Boyd
 * 
 */
public class OpenStreetMapTileProviderDirectTest extends AndroidTestCase {

	MapTileProviderBasic mProvider;

	@Override
	protected void setUp() throws Exception {

		mProvider = new MapTileProviderBasic(getContext());

		super.setUp();
	}

	public void test_getMapTile_not_found() {
		final MapTile tile = new MapTile(2, 3, 4);

		final Drawable drawable = mProvider.getMapTile(tile);

		assertNull("Expect tile to be null", drawable);
	}

	public void test_getMapTile_found() throws RemoteException, FileNotFoundException {
		final MapTile tile = new MapTile(2, 3, 4);

		// create a bitmap, draw something on it, write it to a file and put it in the cache
		final String path = "/sdcard/osmdroid/OpenStreetMapTileProviderTest.png";
		final Bitmap bitmap1 = Bitmap.createBitmap(60, 30, Config.ARGB_8888);
		bitmap1.eraseColor(Color.YELLOW);
		final Canvas canvas = new Canvas(bitmap1);
		canvas.drawText("test", 10, 20, new Paint());
		final FileOutputStream fos = new FileOutputStream(path);
		bitmap1.compress(CompressFormat.PNG, 100, fos);

		final MapTileRequestState state = new MapTileRequestState(tile,
				new MapTileModuleProviderBase[] {}, mProvider);
		mProvider.mapTileRequestCompleted(state, TileSourceFactory.MAPNIK.getDrawable(path));

		// do the test
		final Drawable drawable = mProvider.getMapTile(tile);
		assertNotNull("Expect tile to be not null", drawable);
		assertTrue("Expect instance of BitmapDrawable", drawable instanceof BitmapDrawable);
		final Bitmap bitmap2 = ((BitmapDrawable) drawable).getBitmap();
		assertNotNull("Expect tile to be not null", bitmap2);

		// compare a few things to see if it's the same bitmap
		assertEquals("Compare config", bitmap1.getConfig(), bitmap2.getConfig());
		assertEquals("Compare width", bitmap1.getWidth(), bitmap2.getWidth());
		assertEquals("Compare height", bitmap1.getHeight(), bitmap2.getHeight());

		// compare the total thing
		final ByteBuffer bb1 = ByteBuffer.allocate(bitmap1.getWidth() * bitmap1.getHeight() * 4);
		bitmap1.copyPixelsToBuffer(bb1);
		final ByteBuffer bb2 = ByteBuffer.allocate(bitmap2.getWidth() * bitmap2.getHeight() * 4);
		bitmap2.copyPixelsToBuffer(bb2);
		assertEquals("Compare pixels", bb1, bb2);
	}
}

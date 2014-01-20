package org.osmdroid;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

/**
 * @author Neil Boyd
 *
 */
public class ContextResourceProxyTest extends AndroidTestCase {

	public void test_getString() {
		final ResourceProxy rp = new ResourceProxyImpl(getContext());
		final String mapnik = rp.getString(ResourceProxy.string.mapnik);
		assertEquals("Got string okay", "Mapnik", mapnik);
	}

	public void test_getBitmap() {
		final ResourceProxy rp = new ResourceProxyImpl(getContext());
		final Bitmap center = rp.getBitmap(ResourceProxy.bitmap.person);
		assertNotNull("Got bitmap okay", center);
	}

	public void test_getBitmap_compare_with_default() {
		final ResourceProxy contextResourceProxy = new ResourceProxyImpl(getContext());
		final Bitmap contextBitmap = contextResourceProxy.getBitmap(ResourceProxy.bitmap.person);

		final ResourceProxy defaultResourceProxy = new DefaultResourceProxyImpl(getContext());
		final Bitmap defaultBitmap = defaultResourceProxy.getBitmap(ResourceProxy.bitmap.person);
		// FIXME this throws an exception

		// compare a few things to see if they're the same bitmap
		assertEquals("Compare config", contextBitmap.getConfig(), defaultBitmap.getConfig());
		assertEquals("Compare width", contextBitmap.getWidth(), defaultBitmap.getWidth());
		assertEquals("Compare height", contextBitmap.getHeight(), defaultBitmap.getHeight());

		// compare the total thing
		final ByteBuffer bb1 = ByteBuffer.allocate(contextBitmap.getWidth()
				* contextBitmap.getHeight() * 4);
		contextBitmap.copyPixelsToBuffer(bb1);
		final ByteBuffer bb2 = ByteBuffer.allocate(defaultBitmap.getWidth()
				* defaultBitmap.getHeight() * 4);
		defaultBitmap.copyPixelsToBuffer(bb2);
		assertEquals("Compare pixels", bb1, bb2);
	}
}

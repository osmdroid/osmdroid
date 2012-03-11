package org.osmdroid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import android.graphics.Bitmap;

public class DefaultResourceProxyTest {

	@Test
	public void test_getString() {
		final ResourceProxy rp = new DefaultResourceProxyImpl(null);
		final String mapnik = rp.getString(ResourceProxy.string.mapnik);
		assertEquals("Got string okay", "Mapnik", mapnik);
	}

	@Test(expected = RuntimeException.class)
	public void test_getBitmap_stub() {
		final ResourceProxy rp = new DefaultResourceProxyImpl(null);
		final Bitmap center = rp.getBitmap(ResourceProxy.bitmap.person);
		assertNotNull("Got bitmap okay", center);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_getBitmap_not_found() {
		final ResourceProxy rp = new DefaultResourceProxyImpl(null);
		final Bitmap center = rp.getBitmap(ResourceProxy.bitmap.unknown);
		assertNotNull("Got bitmap okay", center);
	}
}

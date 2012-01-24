package org.osmdroid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import android.graphics.Bitmap;

/**
 * @author Neil Boyd
 * 
 */
public class DefaultResourceProxyTest {

	@Test
	public void test_getString() {
		final ResourceProxy rp = new DefaultResourceProxyImpl(null);
		final String osmarender = rp.getString(ResourceProxy.string.osmarender);
		assertEquals("Got string okay", "Osmarender", osmarender);
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

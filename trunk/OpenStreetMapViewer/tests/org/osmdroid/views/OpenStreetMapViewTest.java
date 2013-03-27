package org.osmdroid.views;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;

/**
 * @author Neil Boyd
 * 
 */
public class OpenStreetMapViewTest extends AndroidTestCase {

	private static final int WIDTH = 300;
	private static final int HEIGHT = 500;

	private MapView mOpenStreetMapView;

	@Override
	protected void setUp() throws Exception {

		// isolated context so that we can't bind to the remote service,
		// but don't isolated from system services because we need them
		final Context context = new IsolatedContext(null, getContext()) {
			@Override
			public Object getSystemService(final String pName) {
				return getContext().getSystemService(pName);
			}
		};

		// final String cloudmadeKey = getCloudmadeKey(applicationContext);
		final MapTileProviderBasic mTileProvider = new MapTileProviderBasic(context);

		mOpenStreetMapView = new MapView(context, 256, new DefaultResourceProxyImpl(context),
				mTileProvider);
		final Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Config.RGB_565);
		final Canvas canvas = new Canvas(bitmap);
		mOpenStreetMapView.dispatchDraw(canvas);

		super.setUp();
	}

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	public void test_toMapPixels_0_0() {

		final GeoPoint zz = new GeoPoint(0, 0);
		mOpenStreetMapView.getController().setCenter(zz);
		mOpenStreetMapView.getController().setZoom(8);
		final Projection projection = mOpenStreetMapView.getProjection();

		final Point point = projection.toMapPixels(zz, null);

		final Point expected = new Point(0, 0);
		assertEquals("TODO describe test", expected, point);
	}

	/**
	 * This test was retrospectively added based on current implementation. TODO a manual
	 * calculation and verify that this test gives the correct result.
	 */
	public void test_toMapPixels_Hannover() {

		final GeoPoint hannover = new GeoPoint(52370816, 9735936);
		mOpenStreetMapView.getController().setCenter(hannover);
		mOpenStreetMapView.getController().setZoom(8);
		final Projection projection = mOpenStreetMapView.getProjection();

		final Point point = projection.toMapPixels(hannover, null);

		final Point expected = new Point(1772, -11231);
		assertEquals("TODO describe test", expected, point);
	}
}

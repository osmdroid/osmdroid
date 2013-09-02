package org.osmdroid.google.wrapper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;

/**
 * A wrapper for the Google {@link com.google.android.maps.MapController} class.
 * This implements {@link IMapController}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapController}.
 *
 * @author Neil Boyd
 *
 */
public class MapController implements IMapController {

	private final com.google.android.maps.MapController mController;

	public MapController(final com.google.android.maps.MapController pController) {
		mController = pController;
	}

	@Override
	public void animateTo(final IGeoPoint pGeoPoint) {
		mController.animateTo(new com.google.android.maps.GeoPoint(pGeoPoint.getLatitudeE6(), pGeoPoint.getLongitudeE6()));
	}

	@Override
	public void setCenter(final IGeoPoint pGeoPoint) {
		mController.setCenter(new com.google.android.maps.GeoPoint(pGeoPoint.getLatitudeE6(), pGeoPoint.getLongitudeE6()));
	}

	@Override
	public int setZoom(final int pZoomLevel) {
		return mController.setZoom(pZoomLevel);
	}

	@Override
	public boolean zoomIn() {
		return mController.zoomIn();
	}

	@Override
	public boolean zoomInFixing(final int xPixel, final int yPixel) {
		return mController.zoomInFixing(xPixel, yPixel);
	}

	@Override
	public boolean zoomOut() {
		return mController.zoomOut();
	}

	@Override
	public boolean zoomOutFixing(final int xPixel, final int yPixel) {
		return mController.zoomOutFixing(xPixel, yPixel);
	}

	@Override
	public void zoomToSpan(final int pLatSpanE6, final int pLonSpanE6) {
		mController.zoomToSpan(pLatSpanE6, pLonSpanE6);
	}

	@Override
	public void scrollBy(int x, int y) {
		mController.scrollBy(x, y);
	}

	@Override
	public void stopAnimation(boolean jumpToFinish) {
		mController.stopAnimation(jumpToFinish);
	}

	@Override
	public void stopPanning() {
		mController.stopPanning();
	}

}

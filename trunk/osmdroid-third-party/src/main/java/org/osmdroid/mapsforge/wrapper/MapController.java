package org.osmdroid.mapsforge.wrapper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;

/**
 * A wrapper for the Google {@link org.mapsforge.android.maps.MapController} class.
 * This implements {@link IMapController}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapController}.
 *
 * @author Neil Boyd
 *
 */
public class MapController implements IMapController {

	private final org.mapsforge.android.maps.MapController mController;

	public MapController(final org.mapsforge.android.maps.MapController pController) {
		mController = pController;
	}

	@Override
	public void animateTo(final IGeoPoint pGeoPoint) {
		// TODO call animateTo if they define it
		mController.setCenter(new org.mapsforge.core.GeoPoint(pGeoPoint.getLatitudeE6(), pGeoPoint.getLongitudeE6()));
	}

	@Override
	public void setCenter(final IGeoPoint pGeoPoint) {
		mController.setCenter(new org.mapsforge.core.GeoPoint(pGeoPoint.getLatitudeE6(), pGeoPoint.getLongitudeE6()));
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
		// TODO call zoomInFixing if they define it
		return mController.zoomIn();
	}

	@Override
	public boolean zoomOut() {
		return mController.zoomOut();
	}

	@Override
	public boolean zoomOutFixing(final int xPixel, final int yPixel) {
		// TODO call zoomOutFixing if they define it
		return mController.zoomOut();
	}

	@Override
	public void zoomToSpan(final int pLatSpanE6, final int pLonSpanE6) {
		// TODO call zoomToSpan if they define it
		// mController.zoomToSpan(pLatSpanE6, pLonSpanE6);
	}

}

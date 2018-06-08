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
@Deprecated
public class MapController implements IMapController {

	private final com.google.android.maps.MapController mController;

	public MapController(final com.google.android.maps.MapController pController) {
		mController = pController;
	}

	@Override
	public void animateTo(final IGeoPoint pGeoPoint) {
		mController.animateTo(new com.google.android.maps.GeoPoint((int)(pGeoPoint.getLatitude()*1E6), (int)(pGeoPoint.getLongitude()*1E6)));
	}

	@Override
	public void animateTo(int x, int y) {
		mController.animateTo(new com.google.android.maps.GeoPoint((y), (x)));
	}

	@Override
	public void setCenter(final IGeoPoint pGeoPoint) {
		mController.setCenter(new com.google.android.maps.GeoPoint((int)(pGeoPoint.getLatitude()), (int)(pGeoPoint.getLongitude())));
	}

	@Override
	public int setZoom(final int pZoomLevel) {
		return mController.setZoom(pZoomLevel);
	}

	/**
	 * @since 6.0
	 */
	@Override
	public double setZoom(final double pZoomLevel) {
		return setZoom((int)pZoomLevel);
	}

	@Override
	public boolean zoomIn() {
		return mController.zoomIn();
	}

	@Override
	public boolean zoomIn(Long animationSpeed) {
		return zoomIn();
	}

	@Override
	public boolean zoomInFixing(int xPixel, int yPixel, Long zoomAnimation) {
		return this.zoomInFixing(xPixel,yPixel);
	}

	@Override
	public boolean zoomInFixing(final int xPixel, final int yPixel) {
		return mController.zoomInFixing(xPixel, yPixel);
	}

	@Override
	public boolean zoomOut(Long animationSpeed) {
		return zoomOut();
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
	public boolean zoomTo(int zoomLevel) {
		return setZoom(zoomLevel) > 0;
	}

	/**
	 * @since 6.0
	 */
	@Override
	public boolean zoomTo(final double pZoomLevel) {
		return zoomTo((int)pZoomLevel);
	}

	@Override
	public boolean zoomTo(int zoomLevel, Long animationSpeed) {
		return zoomTo(zoomLevel);
	}

	@Override
	public boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel, Long zoomAnimationSpeed) {
		return this.zoomToFixing(zoomLevel, xPixel,yPixel);
	}

	@Override
	public boolean zoomTo(double pZoomLevel, Long animationSpeed) {
		return this.zoomTo((int)pZoomLevel);
	}

	@Override
	public boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel) {
		return setZoom(zoomLevel) > 0;
	}

	@Override
	public boolean zoomToFixing(double zoomLevel, int xPixel, int yPixel, Long zoomAnimationSpeed) {
		return this.zoomToFixing((int)zoomLevel,xPixel,yPixel);
	}

	/**
	 * @since 6.0
	 */
	@Override
	public boolean zoomToFixing(final double pZoomLevel, final int pXPixel, final int pYPixel) {
		return zoomToFixing((int)pZoomLevel, pXPixel, pYPixel);
	}

	@Override
	public void zoomToSpan(final int pLatSpanE6, final int pLonSpanE6) {
		mController.zoomToSpan(pLatSpanE6, pLonSpanE6);
	}
//	@Override
	public void zoomToSpan(final double pLatSpan, final double pLonSpan) {
		mController.zoomToSpan((int)(pLatSpan*1E6), (int)(pLonSpan*1E6));
	}

	@Override
	public void animateTo(IGeoPoint point, Double pZoom, Long pSpeed) {

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

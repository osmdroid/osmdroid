package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository of common methods for Polyline and Polygon
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public abstract class PolyOverlayWithIW extends OverlayWithIW {

	protected LinearRing mOutline;
	protected List<LinearRing> mHoles = new ArrayList<>();
	protected final Paint mOutlinePaint = new Paint();
	protected Paint mFillPaint;
	private List<MilestoneManager> mMilestoneManagers = new ArrayList<>();
	private GeoPoint mInfoWindowLocation;

	private final LineDrawer mLineDrawer;
	protected final Path mPath;
	protected float mDensity = 1.0f;
	protected List<GeoPoint> mOriginalPoints = new ArrayList<>();

	protected PolyOverlayWithIW(final MapView pMapView, final boolean pUsePath, final boolean pClosePath) {
		super();
		if (pMapView != null) {
			setInfoWindow(pMapView.getRepository().getDefaultPolylineInfoWindow());
			mDensity = pMapView.getContext().getResources().getDisplayMetrics().density;
		}
		if (pUsePath) {
			mPath = new Path();
			mLineDrawer = null;
			mOutline = new LinearRing(mPath, pClosePath);
		} else {
			mPath = null;
			mLineDrawer = new LineDrawer(256);
			mOutline = new LinearRing(mLineDrawer);
			////mOutline.clearPath();
			mLineDrawer.setPaint(mOutlinePaint);
		}
	}

	/**
	 * Pass style down to LineDrawer class.
	 * @param pStyle color mapping style
	 */
	public void setStyle(final PolylineStyle pStyle) {
		mLineDrawer.setPolylineStyle(pStyle);
	}

	public void setVisible(boolean visible){
		setEnabled(visible);
	}

	public boolean isVisible(){
		return isEnabled();
	}

	/**
	 * @return the Paint used for the outline. This allows to set advanced Paint settings.
	 */
	public Paint getOutlinePaint(){
		return mOutlinePaint;
	}

	/**
	 * @return the Paint used for the filling. This allows to set advanced Paint settings.
	 */
	protected Paint getFillPaint() {
		return mFillPaint;
	}

	/**
	 * Sets whether to draw each segment of the line as a geodesic or not.
	 * Warning: it takes effect only if set before setting the points in the Polyline.
	 */
	public void setGeodesic(boolean geodesic) {
		mOutline.setGeodesic(geodesic);
	}

	public boolean isGeodesic() {
		return mOutline.isGeodesic();
	}

	/**
	 * Set the InfoWindow to be used.
	 * Default is a BasicInfoWindow, with the layout named "bonuspack_bubble".
	 * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow.
	 * If you don't want any InfoWindow to open, you can set it to null.
	 */
	public void setInfoWindow(InfoWindow infoWindow){
		if (mInfoWindow != null){
			if (mInfoWindow.getRelatedObject()==this)
				mInfoWindow.setRelatedObject(null);
		}
		mInfoWindow = infoWindow;
	}

	/**
	 * Show the infowindow, if any. It will be opened either at the latest location, if any,
	 * or to a default location computed by setDefaultInfoWindowLocation method.
	 * Note that you can manually set this location with: setInfoWindowLocation
	 */
	public void showInfoWindow() {
		if (mInfoWindow != null && mInfoWindowLocation != null)
			mInfoWindow.open(this, mInfoWindowLocation, 0, 0);
	}

	/**
	 * Sets the info window anchor point to a geopoint location
	 */
	public void setInfoWindowLocation(GeoPoint location) {
		mInfoWindowLocation = location;
	}

	/**
	 * @return the geopoint location where the infowindow should point at.
	 * Doesn't matter if the infowindow is currently opened or not.
	 */
	public GeoPoint getInfoWindowLocation() {
		return mInfoWindowLocation;
	}

	public void setMilestoneManagers(final List<MilestoneManager> pMilestoneManagers) {
		if (pMilestoneManagers == null) {
			if (mMilestoneManagers.size() > 0) {
				mMilestoneManagers.clear();
			}
		} else {
			mMilestoneManagers = pMilestoneManagers;
		}
	}

	/**
	 * @return aggregate distance (in meters)
	 */
	public double getDistance() {
		return mOutline.getDistance();
	}

	/** Internal method used to ensure that the infowindow will have a default position in all cases,
	 * so that the user can call showInfoWindow even if no tap occured before.
	 * Currently, set the position on the center of the polygon bounding box.
	 */
	protected void setDefaultInfoWindowLocation() {
		int s = mOutline.getPoints().size();
		if (s == 0){
			mInfoWindowLocation = new GeoPoint(0.0, 0.0);
			return;
		}
		//TODO: as soon as the polygon bounding box will be a class member, don't compute it again here.
		mInfoWindowLocation = mOutline.getCenter(null);
	}

	@Override
	public void draw(final Canvas pCanvas, final Projection pProjection) {
		if (mPath != null) {
			drawWithPath(pCanvas, pProjection);
		} else {
			drawWithLines(pCanvas, pProjection);
		}
	}

	private void drawWithPath(final Canvas pCanvas, final Projection pProjection) {
		mPath.rewind();

		mOutline.setClipArea(pProjection);
		final PointL offset = mOutline.buildPathPortion(pProjection, null, mMilestoneManagers.size() > 0);
		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.init();
			milestoneManager.setDistances(mOutline.getDistances());
			for (final PointL point : mOutline.getPointsForMilestones()) {
				milestoneManager.add(point.x, point.y, 0);
			}
			milestoneManager.end();
		}

		if (mHoles != null) {
			for (final LinearRing hole : mHoles) {
				hole.setClipArea(pProjection);
				hole.buildPathPortion(pProjection, offset, mMilestoneManagers.size() > 0);
			}
			mPath.setFillType(Path.FillType.EVEN_ODD); //for correct support of holes
		}

		if (mFillPaint != null) {
			pCanvas.drawPath(mPath, mFillPaint);
		}
		pCanvas.drawPath(mPath, mOutlinePaint);

		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.draw(pCanvas);
		}

		if (isInfoWindowOpen() && mInfoWindow!=null && mInfoWindow.getRelatedObject() == this) {
			mInfoWindow.draw();
		}
	}

	private void drawWithLines(final Canvas pCanvas, final Projection pProjection) {
		mLineDrawer.setCanvas(pCanvas);
		mOutline.setClipArea(pProjection);
		mOutline.buildLinePortion(pProjection, mMilestoneManagers.size() > 0);
		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.init();
			milestoneManager.setDistances(mOutline.getDistances());
			for (final PointL point : mOutline.getPointsForMilestones()) {
				milestoneManager.add(point.x, point.y, 0);
			}
			milestoneManager.end();
		}

		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.draw(pCanvas);
		}
		if (isInfoWindowOpen() && mInfoWindow!=null && mInfoWindow.getRelatedObject() == this) {
			mInfoWindow.draw();
		}
	}

	@Override
	public void onDetach(MapView mapView) {
		mOutline = null;
		mHoles.clear();
		mMilestoneManagers.clear();
		mOriginalPoints = null;
		onDestroy();
	}
}
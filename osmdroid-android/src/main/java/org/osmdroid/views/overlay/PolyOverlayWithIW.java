package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
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
	protected Paint mOutlinePaint = new Paint();
	protected Paint mFillPaint;
	private final List<PaintList> mOutlinePaintLists = new ArrayList<>();
	private List<MilestoneManager> mMilestoneManagers = new ArrayList<>();
	private GeoPoint mInfoWindowLocation;

	private final LineDrawer mLineDrawer;
	protected final Path mPath;
	protected float mDensity = 1.0f;

	/**
	 * @since 6.2.0
	 */
	private boolean mIsPaintOrPaintList = true;

    /**
     * @since 6.2.0
     */
	private int mDowngradeMaximumPixelSize;
	private boolean mDowngradeDisplay;
	private final Point mDowngradeTopLeft = new Point();
	private final Point mDowngradeBottomRight = new Point();
	private final PointL mDowngradeCenter = new PointL();
	private final PointL mDowngradeOffset = new PointL();

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
			mOutline = new LinearRing(mLineDrawer, pClosePath);
			////mOutline.clearPath();
			mLineDrawer.setPaint(mOutlinePaint);
		}
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
		mIsPaintOrPaintList = true;
		return mOutlinePaint;
	}

	/**
	 * assuming if someone uses this method, someone wants to use List<PaintList>
	 *     instead of mere Paint
	 */
	public List<PaintList> getOutlinePaintLists(){
		mIsPaintOrPaintList = false;
		return mOutlinePaintLists;
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
		if (mInfoWindowLocation == null) {
			mInfoWindowLocation = new GeoPoint(0., 0);
		}
		mOutline.getCenter(mInfoWindowLocation);
	}

	@Override
	public void draw(final Canvas pCanvas, final Projection pProjection) {
		if (mDowngradeMaximumPixelSize > 0) {
			if (!isWorthDisplaying(pProjection, mDowngradeMaximumPixelSize)) {
				if (mDowngradeDisplay) {
					displayDowngrade(pCanvas, pProjection);
				}
				return;
			}
		}

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
				milestoneManager.add(point.x, point.y);
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
		boolean storePoints = mMilestoneManagers.size() > 0;
		if (mIsPaintOrPaintList) {
			final Paint paint = getOutlinePaint();
			mLineDrawer.setPaint(paint);
			mOutline.buildLinePortion(pProjection, storePoints);
		} else {
			for (final PaintList paintList : getOutlinePaintLists()) {
				mLineDrawer.setPaint(paintList);
				mOutline.buildLinePortion(pProjection, storePoints);
				storePoints = false;
			}
		}
		for (final MilestoneManager milestoneManager : mMilestoneManagers) {
			milestoneManager.init();
			milestoneManager.setDistances(mOutline.getDistances());
			for (final PointL point : mOutline.getPointsForMilestones()) {
				milestoneManager.add(point.x, point.y);
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
		mOutline.clear();
		mOutline = null;
		mHoles.clear();
		mMilestoneManagers.clear();
		onDestroy();
	}

	/**
	 * @since 6.2.0
	 */
    @Override
    public BoundingBox getBounds() {
    	return mOutline.getBoundingBox();
    }

	/**
	 * @since 6.2.0
	 * Used to be in {@link Polygon} and {@link Polyline}
	 * Set the points of the outline.
	 * Note that a later change in the original points List will have no effect.
	 * To remove/change points, you must call setPoints again.
	 * If geodesic mode has been set, the long segments will follow the earth "great circle".
	 */
	public void setPoints(final List<GeoPoint> points) {
		mOutline.setPoints(points);
		setDefaultInfoWindowLocation();
	}

	/**
	 * @since 6.2.0
	 * Used to be in {@link Polygon} and {@link Polyline}
	 * Add the point at the end of the outline
	 * If geodesic mode has been set, the long segments will follow the earth "great circle".
	 */
	public void addPoint(GeoPoint p){
		mOutline.addPoint(p);
	}

	/**
	 * @since 6.2.0
	 * @return a direct link to the list of polygon's vertices,
	 * which are the original points if we didn't use the geodesic feature
	 * Warning: changes on this list may cause strange results on the display.
	 */
	public List<GeoPoint> getActualPoints(){
		return mOutline.getPoints();
	}

    /**
     * @since 6.2.0
     * If the size of the Poly (width or height) projected on the screen is lower than the parameter,
     * the Poly won't be displayed as a real Poly, but downgraded to a rectangle
     * (or not displayed at all, depending on {@link #setDowngradeDisplay(boolean)}
     */
    public void setDowngradeMaximumPixelSize(final int pDowngradeMaximumPixelSize) {
        mDowngradeMaximumPixelSize = pDowngradeMaximumPixelSize;
    }

    /**
     * @since 6.2.0
     * See {@link #setDowngradeMaximumPixelSize(int)}
     */
    public void setDowngradeDisplay(final boolean pDowngradeDisplay) {
        mDowngradeDisplay = pDowngradeDisplay;
    }

    /**
	 * @since 6.2.0
	 */
	private boolean isWorthDisplaying(final Projection pProjection, final int pMinimumPixelSize) {
		final BoundingBox boundingBox = getBounds();
		pProjection.toPixels(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()), mDowngradeTopLeft);
		pProjection.toPixels(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()), mDowngradeBottomRight);
		final double worldSize = pProjection.getWorldMapSize();
		final long right = Math.round(mOutline.getCloserValue(mDowngradeTopLeft.x, mDowngradeBottomRight.x, worldSize));
		final long bottom = Math.round(mOutline.getCloserValue(mDowngradeTopLeft.y, mDowngradeBottomRight.y, worldSize));
		if (Math.abs(mDowngradeTopLeft.x - mDowngradeBottomRight.x) < pMinimumPixelSize) {
			return false;
		}
		if (Math.abs(mDowngradeTopLeft.x - right) < pMinimumPixelSize) {
			return false;
		}
		if (Math.abs(mDowngradeTopLeft.y - mDowngradeBottomRight.y) < pMinimumPixelSize) {
			return false;
		}
		if (Math.abs(mDowngradeTopLeft.y - bottom) < pMinimumPixelSize) {
			return false;
		}
		return true;
	}

	/**
	 * @since 6.2.0
	 */
	private void displayDowngrade(final Canvas pCanvas, final Projection pProjection) {
		final BoundingBox boundingBox = mOutline.getBoundingBox();
		pProjection.toPixels(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()), mDowngradeTopLeft);
		pProjection.toPixels(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()), mDowngradeBottomRight);
		final double worldSize = pProjection.getWorldMapSize();
		long left = mDowngradeTopLeft.x;
		long top = mDowngradeTopLeft.y;
		final long right = Math.round(mOutline.getCloserValue(left, mDowngradeBottomRight.x, worldSize));
		final long bottom = Math.round(mOutline.getCloserValue(top, mDowngradeBottomRight.y, worldSize));
		final long width;
		if (left == right) {
			width = 1;
		} else if (left > right) {
			width = left - right;
			left = right;
		} else {
			width = right - left;
		}
		final long height;
		if (top == bottom) {
			height = 1;
		} else if (top > bottom) {
			height = top - bottom;
			top = bottom;
		} else {
			height = bottom - top;
		}
		mDowngradeCenter.set(left + width / 2, top + height / 2);
		mOutline.getBestOffset(pProjection, mDowngradeOffset, mDowngradeCenter);
		left += mDowngradeOffset.x;
		top += mDowngradeOffset.y;

		Paint paint = null;
		if (mIsPaintOrPaintList) {
			paint = getOutlinePaint();
		} else if (getOutlinePaintLists().size() > 0) {
			final PaintList paintList = getOutlinePaintLists().get(0);
			paint = paintList.getPaint();
			if (paint == null) { // polychromatic
				paint = paintList.getPaint(0, left, top, left + width, top + height);
			}
		}
		if (mFillPaint != null) {
			pCanvas.drawRect(left, top, left + width, top + height, mFillPaint);
		}
		if (paint != null) {
			pCanvas.drawRect(left, top, left + width, top + height, paint);
		}
	}
}
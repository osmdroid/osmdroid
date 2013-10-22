package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * @author Viesturs Zarins
 * @author Martin Pearman
 * @author M.Kergall
 * 
 * This class draws a polygon.
 */
public class PolygonOverlay extends Overlay {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	/**
	 * Stores points, converted to the map projection.
	 */
	private ArrayList<Point> mPoints;

	/**
	 * Number of points that have precomputed values.
	 */
	private int mPointsPrecomputed;

	/**
	 * Paint settings.
	 */
	protected Paint mFillPaint = new Paint();
	protected Paint mOutlinePaint = null;

	private final Path mPath = new Path();

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	// bounding rectangle for the current line segment.
	//private final Rect mLineBounds = new Rect();

	// ===========================================================
	// Constructors
	// ===========================================================

	public PolygonOverlay(final int color, final Context ctx) {
		this(color, new DefaultResourceProxyImpl(ctx));
	}

	public PolygonOverlay(final int color, final ResourceProxy resourceProxy) {
		super(resourceProxy);
		this.mFillPaint.setColor(color);
		this.mFillPaint.setStyle(Paint.Style.FILL);
		this.clearPath();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Paint getFillPaint() {
		return mFillPaint;
	}

	public Paint getOutlinePaint() {
		return mOutlinePaint;
	}

	public void setFillPaint(final Paint pPaint) {
		mFillPaint = pPaint;
	}

	public void setOutlinePaint(final Paint pPaint) {
		mOutlinePaint = pPaint;
		if (mOutlinePaint != null)
			mOutlinePaint.setStyle(Paint.Style.STROKE);
	}
	
	public void clearPath() {
		this.mPoints = new ArrayList<Point>();
		this.mPointsPrecomputed = 0;
	}

	public void addPoint(final IGeoPoint pt) {
		this.addPoint(pt.getLatitudeE6(), pt.getLongitudeE6());
	}

	public void addPoint(final int latitudeE6, final int longitudeE6) {
		this.mPoints.add(new Point(latitudeE6, longitudeE6));
	}

	public int getNumberOfPoints() {
		return this.mPoints.size();
	}

	/**
	 * This method draws the line. Note - highly optimized to handle long paths, proceed with care.
	 * Should be fine up to 10K points.
	 */
	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

		if (shadow) {
			return;
		}

		final int size = this.mPoints.size();
		if (size < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		// precompute new points to the intermediate projection.
		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mPoints.get(this.mPointsPrecomputed);
			pj.toMapPixelsProjected(pt.x, pt.y, pt);

			this.mPointsPrecomputed++;
		}

		Point screenPoint0 = null; // points on screen
		Point screenPoint1;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		// clipping rectangle in the intermediate projection, to avoid performing projection.
		//final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());
		
		mPath.rewind();
		projectedPoint0 = this.mPoints.get(size - 1);
		//mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = this.mPoints.get(i);
			//mLineBounds.union(projectedPoint1.x, projectedPoint1.y);

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) {
				screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
				mPath.moveTo(screenPoint0.x, screenPoint0.y);
			}

			screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

			// skip this point, too close to previous point
			if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				continue;
			}

			mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
			//mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);
		}

		if (mFillPaint != null)
			canvas.drawPath(mPath, this.mFillPaint);
		if (mOutlinePaint != null)
			canvas.drawPath(mPath, this.mOutlinePaint);
	}
}

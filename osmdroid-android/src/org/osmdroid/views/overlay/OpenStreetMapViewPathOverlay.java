package org.osmdroid.views.overlay;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.OpenStreetMapView;
import org.osmdroid.views.OpenStreetMapView.OpenStreetMapViewProjection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * 
 * @author Viesturs Zarins
 * 
 *         This class draws a path line in given color.
 */
public class OpenStreetMapViewPathOverlay extends OpenStreetMapViewOverlay {
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
	protected final Paint mPaint = new Paint();

	/**
	 * Point cache for Canvas.drawLines.
	 */
	static final int POINT_BUFFER_SIZE = 256; // should be a multiple of 4
	private final float[] mPointBuffer = new float[POINT_BUFFER_SIZE];

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapViewPathOverlay(final int color, final Context ctx) {
		this(color, new DefaultResourceProxyImpl(ctx));
	}

	public OpenStreetMapViewPathOverlay(final int color, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		this.mPaint.setColor(color);
		this.mPaint.setStrokeWidth(2.0f);

		this.clearPath();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setColor(final int color) {
		this.mPaint.setColor(color);
	}

	public void setAlpha(final int a) {
		this.mPaint.setAlpha(a);
	}

	public void clearPath() {
		this.mPoints = new ArrayList<Point>();
		this.mPointsPrecomputed = 0;
	}

	public void addPoint(final GeoPoint pt) {
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
	protected void onDraw(final Canvas canvas, final OpenStreetMapView mapView) {
		if (this.mPoints.size() < 2) {
			// nothing to paint
			return;
		}

		final OpenStreetMapViewProjection pj = mapView.getProjection();

		// precompute new points to the intermediate projection.
		final int size = this.mPoints.size();

		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mPoints.get(this.mPointsPrecomputed);
			pj.toMapPixelsProjected(pt.x, pt.y, pt);

			this.mPointsPrecomputed++;
		}

		Point screenPoint0 = null; // points on screen
		Point screenPoint1 = null;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		final float[] buffer = this.mPointBuffer;
		int bufferCount = 0;
		final Rect clipBounds = pj.fromPixelsToProjected(canvas.getClipBounds()); // clipping
																					// rectangle in
																					// the
																					// intermediate
																					// projection,
																					// to avoid
																					// performing
																					// projection.
		final Rect lineBounds = new Rect(); // bounding rectangle for the current line segment.

		projectedPoint0 = this.mPoints.get(size - 1);
		lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = this.mPoints.get(i);
			lineBounds.union(projectedPoint1.x, projectedPoint1.y);

			if (!Rect.intersects(clipBounds, lineBounds)) {
				// skip this line, move to next point
				projectedPoint0 = projectedPoint1;
				screenPoint0 = null;
				continue;
			}

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) {
				screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
			}

			screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

			// skip this point, too close to previous point
			if (Math.abs(screenPoint1.x - screenPoint0.x)
					+ Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				continue;
			}

			// add new line to buffer
			buffer[bufferCount] = screenPoint0.x;
			buffer[bufferCount + 1] = screenPoint0.y;
			buffer[bufferCount + 2] = screenPoint1.x;
			buffer[bufferCount + 3] = screenPoint1.y;
			bufferCount += 4;

			if (bufferCount == POINT_BUFFER_SIZE) {
				canvas.drawLines(buffer, this.mPaint);
				bufferCount = 0;
			}

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
			lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x,
					projectedPoint0.y);
		}

		if (bufferCount > 0) {
			canvas.drawLines(buffer, 0, bufferCount, this.mPaint);
		}
	}

	@Override
	protected void onDrawFinished(final Canvas c, final OpenStreetMapView osmv) {
		// nothing here
	}

}

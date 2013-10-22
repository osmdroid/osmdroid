package org.osmdroid.google.overlay;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import org.osmdroid.api.IGeoPoint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

// based on org.osmdroid.views.overlay.PathOverlay
public class GooglePolylineOverlay extends Overlay {

	private ArrayList<GeoPoint> mPoints;

	protected Paint mPaint = new Paint();

	private final Path mPath = new Path();

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	public GooglePolylineOverlay(final int aColor) {
		this(aColor, 2.0f);
	}

	public GooglePolylineOverlay(final int aColor, final float aWidth) {
		mPaint.setColor(aColor);
		mPaint.setStrokeWidth(aWidth);
		mPaint.setStyle(Paint.Style.STROKE);

		clearPath();
	}

	public void clearPath() {
		mPoints = new ArrayList<GeoPoint>();
	}

	public void addPoints(final IGeoPoint... aPoints) {
		for(final IGeoPoint geoPoint : aPoints) {
			addPoint(geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6());
		}
	}

	public void addPoints(final List<IGeoPoint> aPoints) {
		for(final IGeoPoint point : aPoints) {
			addPoint(point.getLatitudeE6(), point.getLongitudeE6());
		}
	}

	public void addPoint(final int latitudeE6, final int longitudeE6) {
		mPoints.add(new GeoPoint(latitudeE6, longitudeE6));
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		if (shadow) {
			return;
		}

		final int size = mPoints.size();
		if (size < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		Point screenPoint0 = null; // points on screen
		Point screenPoint1;
		GeoPoint projectedPoint0; // points from the points list
		GeoPoint projectedPoint1;

		mPath.rewind();
		projectedPoint0 = mPoints.get(size - 1);

		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = mPoints.get(i);

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) {
				screenPoint0 = pj.toPixels(projectedPoint0, mTempPoint1);
				mPath.moveTo(screenPoint0.x, screenPoint0.y);
			}

			screenPoint1 = pj.toPixels(projectedPoint1, mTempPoint2);

			// skip this point, too close to previous point
			if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				continue;
			}

			mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
		}

		canvas.drawPath(mPath, mPaint);
	}
}

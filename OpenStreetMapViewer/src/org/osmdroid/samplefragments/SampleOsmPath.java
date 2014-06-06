package org.osmdroid.samplefragments;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.drawing.OsmPath;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Bundle;

/**
 * 
 * @author Marc Kurtz
 * 
 */
public class SampleOsmPath extends BaseSampleFragment {

	public static final String TITLE = "OsmPath drawing";

	private static final BoundingBoxE6 sCentralParkBoundingBox;
	private static final Paint sPaint;

	private OsmPathOverlay mOsmPathOverlay;

	static {
		sCentralParkBoundingBox = new BoundingBoxE6(40.796788, -73.949232, 40.768094, -73.981762);

		sPaint = new Paint();
		sPaint.setColor(Color.argb(175, 255, 0, 0));
		sPaint.setStyle(Style.FILL);
	}
	@Override
	public String getSampleTitle() {
		return TITLE;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		mMapView.getController().setZoom(14);
		mMapView.getController().setCenter(sCentralParkBoundingBox.getCenter());

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void addOverlays() {
		super.addOverlays();

		final Context context = getActivity();

		mOsmPathOverlay = new OsmPathOverlay(context);
		mMapView.getOverlayManager().add(mOsmPathOverlay);
	}

	class OsmPathOverlay extends Overlay {

		private int mLastZoomLevel;
		private OsmPath mPath;

		public OsmPathOverlay(Context ctx) {
			super(ctx);
		}

		@Override
		protected void draw(Canvas c, MapView osmv, boolean shadow) {
			if (shadow)
				return;

			final Projection proj = osmv.getProjection();

			if (mPath == null || mLastZoomLevel != proj.getZoomLevel())
				mPath = createPath();

			mLastZoomLevel = proj.getZoomLevel();

			// This allows us to properly position the path without having to rebuild it on every
			// draw cycle
			mPath.onDrawCycle(proj);

			c.drawPath(mPath, sPaint);
		}

		private OsmPath createPath() {
			OsmPath path = new OsmPath();

			final Projection proj = mMapView.getProjection();
			Point p = null;
			p = proj.toPixels(new GeoPoint(sCentralParkBoundingBox.getLatNorthE6(),
					sCentralParkBoundingBox.getLonWestE6()), p);
			path.moveTo(p.x, p.y);
			p = proj.toPixels(new GeoPoint(sCentralParkBoundingBox.getLatNorthE6(),
					sCentralParkBoundingBox.getLonEastE6()), p);
			path.lineTo(p.x, p.y);
			p = proj.toPixels(new GeoPoint(sCentralParkBoundingBox.getLatSouthE6(),
					sCentralParkBoundingBox.getLonEastE6()), p);
			path.lineTo(p.x, p.y);
			p = proj.toPixels(new GeoPoint(sCentralParkBoundingBox.getLatSouthE6(),
					sCentralParkBoundingBox.getLonWestE6()), p);
			path.lineTo(p.x, p.y);
			path.close();

			return path;
		}
	}
}

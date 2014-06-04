package org.osmdroid.samplefragments;

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

	private static final GeoPoint sCentralParkPoint;
	private static final Paint sPaint;

	private OsmPathOverlay mOsmPathOverlay;

	static {
		sCentralParkPoint = new GeoPoint(40.782441, -73.965497);

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

		mMapView.getController().setZoom(13);
		mMapView.getController().setCenter(sCentralParkPoint);

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
			p = proj.toPixels(new GeoPoint(40.782441, -73.965497), p);
			path.moveTo(p.x, p.y);
			p = proj.toPixels(new GeoPoint(40.6, -73.965497), p);
			path.lineTo(p.x, p.y);
			p = proj.toPixels(new GeoPoint(40.6, -73.8), p);
			path.lineTo(p.x, p.y);
			path.close();

			return path;
		}
	}
}

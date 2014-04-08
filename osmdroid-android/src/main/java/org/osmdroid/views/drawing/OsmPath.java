package org.osmdroid.views.drawing;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

import android.graphics.Path;
import android.graphics.Point;

public class OsmPath extends Path {

	private final static GeoPoint sReferenceGeoPoint = new GeoPoint(0, 0);
	protected final Point mReferencePoint = new Point();
	private int mLastZoomLevel = -1;
	
    public OsmPath() {
		super();
	}

	public OsmPath(Path src) {
		super(src);
	}

	public void onDrawCycle(Projection proj) {
		if (mLastZoomLevel != proj.getZoomLevel()) {
			proj.toPixels(sReferenceGeoPoint, mReferencePoint);
			mLastZoomLevel = proj.getZoomLevel();
		} else {
			int x = mReferencePoint.x;
			int y = mReferencePoint.y;
			proj.toPixels(sReferenceGeoPoint, mReferencePoint);
			int deltaX = mReferencePoint.x - x;
			int deltaY = mReferencePoint.y - y;

			offset(deltaX, deltaY);
		}
	}
}

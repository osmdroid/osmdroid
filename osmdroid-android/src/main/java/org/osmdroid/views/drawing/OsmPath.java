package org.osmdroid.views.drawing;

import android.graphics.Path;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

/**
 * Since the osmdroid canvas coordinate system is changing with every scroll, the x/y coordinates of
 * lat/long points is also always changing. Converting from lat/long to pixel values is a
 * potentially expensive operation and shouldn't be performed in the draw() cycle. Instead of
 * recalculating your {@link Path} points every draw cycle, you can use an OsmPath and call
 * {@link #onDrawCycle(Projection)} at the start of your draw call. This will simply shift the Path
 * the proper amount so that it is in the correct pixel position.
 *
 * @author Marc Kurtz
 * @deprecated Use {@link Polyline} or {@link Polygon} instead
 */
@Deprecated
public class OsmPath extends Path {

    private final static GeoPoint sReferenceGeoPoint = new GeoPoint(0, 0);
    protected final Point mReferencePoint = new Point();
    private double mLastZoomLevel = -1;

    public OsmPath() {
        super();
    }

    public OsmPath(Path src) {
        super(src);
    }

    /**
     * Call this method at the beginning of every draw() call.
     */
    public void onDrawCycle(Projection proj) {
        if (mLastZoomLevel != proj.getZoomLevel()) {
            proj.toPixels(sReferenceGeoPoint, mReferencePoint);
            mLastZoomLevel = proj.getZoomLevel();
        }
        int x = mReferencePoint.x;
        int y = mReferencePoint.y;
        proj.toPixels(sReferenceGeoPoint, mReferencePoint);
        int deltaX = mReferencePoint.x - x;
        int deltaY = mReferencePoint.y - y;

        offset(deltaX, deltaY);
    }
}

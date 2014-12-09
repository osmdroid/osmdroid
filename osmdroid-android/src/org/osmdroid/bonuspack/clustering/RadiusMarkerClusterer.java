package org.osmdroid.bonuspack.clustering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import org.osmdroid.bonuspack.clustering.MarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Radius-based Clustering algorithm:
 * create a cluster using the first point from the cloned list.
 * All points that are found within the neighborhood are added to this cluster.
 * Then all the neighbors and the main point are removed from the list of points.
 * It continues until the list is empty.
 *
 * Largely inspired from GridMarkerClusterer by M.Kergall
 *
 * @author sidorovroman92@gmail.com
 */

public class RadiusMarkerClusterer extends MarkerClusterer {

    protected int mMaxClusteringZoomLevel = 17;
    protected int mRadiusInPixels = 100;
    protected double mRadiusInMeters;
    protected Paint mTextPaint;
    private ArrayList<Marker> mClonedMarkers;

    /** cluster icon anchor */
    public float mAnchorU = Marker.ANCHOR_CENTER, mAnchorV = Marker.ANCHOR_CENTER;
    /** anchor point to draw the number of markers inside the cluster icon */
    public float mTextAnchorU = Marker.ANCHOR_CENTER, mTextAnchorV = Marker.ANCHOR_CENTER;

    public RadiusMarkerClusterer(Context ctx) {
        super(ctx);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(15.0f);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
    }

    /** If you want to change the default text paint (color, size, font) */
    public Paint getTextPaint(){
        return mTextPaint;
    }

    /** Set the radius of clustering in pixels. Default is 100px. */
    public void setRadius(int radius){
        mRadiusInPixels = radius;
    }

    /** Set max zoom level with clustering. When zoom is higher or equal to this level, clustering is disabled. 
     * You can put a high value to disable this feature. */
    public void setMaxClusteringZoomLevel(int zoom){
        mMaxClusteringZoomLevel = zoom;
    }

    /** Radius-Based clustering algorithm */
    @Override public ArrayList<StaticCluster> clusterer(MapView mapView) {

        ArrayList<StaticCluster> clusters = new ArrayList<StaticCluster>();
        convertRadiusToMeters(mapView);

        mClonedMarkers = new ArrayList<Marker>(mItems); //shallow copy
        while (!mClonedMarkers.isEmpty()) {
            Marker m = mClonedMarkers.get(0);
            StaticCluster cluster = createCluster(m, mapView);
            clusters.add(cluster);
        }
        return clusters;
    }

    private StaticCluster createCluster(Marker m, MapView mapView) {
        GeoPoint clusterPosition = m.getPosition();

        StaticCluster cluster = new StaticCluster(clusterPosition);
        cluster.add(m);

        mClonedMarkers.remove(m);
        
        if (mapView.getZoomLevel() > mMaxClusteringZoomLevel) {
        	//above max level => block clustering:
        	return cluster;
        }
        
        Iterator<Marker> it = mClonedMarkers.iterator();
        while (it.hasNext()) {
            Marker neighbour = it.next();
            int distance = clusterPosition.distanceTo(neighbour.getPosition());
            if (distance <= mRadiusInMeters) {
                cluster.add(neighbour);
                it.remove();
            }
        }

        return cluster;
    }

    @Override public Marker buildClusterMarker(StaticCluster cluster, MapView mapView) {
        Marker m = new Marker(mapView);
        m.setPosition(cluster.getPosition());
        m.setInfoWindow(null);
        m.setAnchor(mAnchorU, mAnchorV);

        Bitmap finalIcon = Bitmap.createBitmap(mClusterIcon.getWidth(), mClusterIcon.getHeight(), mClusterIcon.getConfig());
        Canvas iconCanvas = new Canvas(finalIcon);
        iconCanvas.drawBitmap(mClusterIcon, 0, 0, null);
        String text = "" + cluster.getSize();
        int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
        iconCanvas.drawText(text,
                mTextAnchorU * finalIcon.getWidth(),
                mTextAnchorV * finalIcon.getHeight() - textHeight / 2,
                mTextPaint);
        m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));

        return m;
    }

    @Override public void renderer(ArrayList<StaticCluster> clusters, Canvas canvas, MapView mapView) {
        for (StaticCluster cluster : clusters) {
            if (cluster.getSize() == 1) {
                //cluster has only 1 marker => use it as it is:
                cluster.setMarker(cluster.getItem(0));
            } else {
                //only draw 1 Marker at Cluster center, displaying number of Markers contained
                Marker m = buildClusterMarker(cluster, mapView);
                cluster.setMarker(m);
            }
        }
    }

    private void convertRadiusToMeters(MapView mapView) {

        Rect mScreenRect = mapView.getIntrinsicScreenRect(null);

        int screenWidth = mScreenRect.right - mScreenRect.left;
        int screenHeight = mScreenRect.bottom - mScreenRect.top;

        BoundingBoxE6 bb = mapView.getBoundingBox();

        double diagonalInMeters = bb.getDiagonalLengthInMeters();
        double diagonalInPixels = Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight);
        double metersInPixel = diagonalInMeters / diagonalInPixels;

        mRadiusInMeters = mRadiusInPixels * metersInPixel;
    }

}

package org.osmdroid.bonuspack.clustering;

import java.util.ArrayList;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LongSparseArray;
import android.view.MotionEvent;

/** 
 * An overlay allowing to perform markers clustering. 
 * Usage: put your markers inside with add(Marker), and add the MarkerClusterer to the map overlays. 
 * Depending on the zoom level, markers will be displayed separately, or grouped. 
 * 
 * Clustering algorithm is grid-based : all markers inside the same cell belong to the same cluster. 
 * The grid size is specified in screen pixels. 
 * 
 * TODO: handle map rotation. 
 * 
 * Largely inspired from (open source) Google Maps Android utility library. 
 * @see https://github.com/googlemaps/android-maps-utils
 * @author M.Kergall
 *
 */
public class MarkerClusterer extends SafeDrawOverlay {

	protected ArrayList<Marker> mItems = new ArrayList<Marker>();
	protected Point mPoint = new Point();
	protected ArrayList<StaticCluster> mClusters = new ArrayList<StaticCluster>();;
	protected int mLastZoomLevel = -1; //impossible value, to force clustering
	protected Bitmap mClusterIcon;
	protected int mGridSize = 50; //in pixels
	protected Paint mTextPaint;
	
	/** cluster icon anchor */
	public float mAnchorU = Marker.ANCHOR_CENTER, mAnchorV = Marker.ANCHOR_CENTER;
	/** anchor point to draw the number of markers inside the cluster icon */
	public float mTextAnchorU = Marker.ANCHOR_CENTER, mTextAnchorV = Marker.ANCHOR_CENTER;

	class StaticCluster {
		private final ArrayList<Marker> mItems = new ArrayList<Marker>();
		private final GeoPoint mCenter;
		Marker mMarker;
		
	    public StaticCluster(GeoPoint center) {
	        mCenter = center;
	    }
	    
	    public GeoPoint getPosition() {
	        return mCenter;
	    }
	    
	    public int getSize() {
	        return mItems.size();
	    }
	    
	    public Marker getItem(int index) {
	        return mItems.get(index);
	    }
	    
	    public boolean add(Marker t) {
	        return mItems.add(t);
	    }
	    
	    public void setMarker(Marker marker){
	    	mMarker = marker;
	    }

	    public Marker getMarker(){
	    	return mMarker;
	    }
	}
	
	public MarkerClusterer(Context ctx) {
		super(ctx);
		mTextPaint = new Paint();
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(17.0f);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setAntiAlias(true);
	}

	/** Set the cluster icon to be drawn when a cluster contains more than 1 marker. 
	 * Default will be the default osmdroid marker icon, which is really inappropriate as a cluster icon. */
	public void setIcon(Bitmap icon){
		mClusterIcon = icon;
	}
	
	/** If you want to change the default text paint (color, size, font) */
	public Paint getTextPaint(){
		return mTextPaint;
	}
	
	/** Add the Marker. Do not add this Marker in the map overlays. */
	public void add(Marker marker){
		mItems.add(marker);
		invalidate();
	}
	
	/** Force a rebuild of clusters at next draw */
	public void invalidate(){
		mLastZoomLevel = -1; 
	}
	
	/** @return the Marker at id (starting at 0) */
	public Marker getItem(int id){
		return mItems.get(id);
	}
	
	/** @return the list of Markers. 
	 * If you then make changes to the items in the list, call invalidate() to force a rebuild of clusters. */
	public ArrayList<Marker> getItems(){
		return mItems;
	}
	
	/** Set the grid size in pixels. */
	public void setGridSize(int gridSize){
		mGridSize = gridSize;
	}

	/** Grid-based clustering algorithm */
	public ArrayList<StaticCluster> clusterer(MapView mapView){
		Rect mScreenRect = mapView.getIntrinsicScreenRect(null);
		
		BoundingBoxE6 bb = mapView.getBoundingBox();
		double latSpan = bb.getLatitudeSpanE6()*1E-6;
		double lonSpan = bb.getLongitudeSpanE6()*1E-6;
		//Log.d("ZOOM", "latSpan="+latSpan+"  lonSpan="+lonSpan);
		
		//convert grid size from pixels to degrees:
		double gridSizeX, gridSizeY;
		gridSizeX = lonSpan * (double)mGridSize / (double)(mScreenRect.right - mScreenRect.left);
		gridSizeY = latSpan * (double)mGridSize / (double)(mScreenRect.bottom - mScreenRect.top);
		
		int numCellsW = (int)(360.0f/gridSizeX);
		//Log.d("ZOOM", "zoomlevel="+mapView.getZoomLevel()+"  cells="+numCellsW);
		//Log.d("ZOOM", "gridSizeX="+gridSizeX+"  gridSizeY="+gridSizeY);
		
		ArrayList<StaticCluster> clusters = new ArrayList<StaticCluster>();
		LongSparseArray<StaticCluster> sparseArray = new LongSparseArray<StaticCluster>();
		for (Marker item:mItems){
			//TODO - add 180° to prevent negative values
			long gridX = (long)(item.getPosition().getLongitude() / gridSizeX);
			long gridY = (long)(item.getPosition().getLatitude() / gridSizeY);
			long coord = numCellsW * gridX + gridY;
			StaticCluster cluster = sparseArray.get(coord);
			//Log.d("ZOOM", "coord="+coord+" =>cluster:"+(cluster==null?"new":"add"));
			if (cluster == null) {
				//GeoPoint clusterCorner = new GeoPoint(gridSizeY*(double)gridY, gridSizeX*(double)gridX);
                cluster = new StaticCluster(item.getPosition() /*clusterCorner*/);
                sparseArray.put(coord, cluster);
                clusters.add(cluster);
			}
            cluster.add(item);
		}
		return clusters;
	}
	
	/** build the marker for a cluster. 
	 * Uses the cluster icon, and adds the number of markers contained. */
	public Marker buildClusterMarker(StaticCluster cluster, MapView mapView){
		Marker m = new Marker(mapView);
		m.setPosition(cluster.getPosition());
		m.setInfoWindow(null);
		m.setAnchor(mAnchorU, mAnchorV);
		Bitmap finalIcon = Bitmap.createBitmap(mClusterIcon.getWidth(), mClusterIcon.getHeight(), mClusterIcon.getConfig());
		Canvas iconCanvas = new Canvas(finalIcon);
		iconCanvas.drawBitmap(mClusterIcon, 0, 0, null);
		String text = ""+cluster.getSize();
	    int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
		iconCanvas.drawText(text, 
				mTextAnchorU*mClusterIcon.getWidth(), 
				mTextAnchorV*mClusterIcon.getHeight() - textHeight/2, 
				mTextPaint);
		m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));
		return m;
	}
	
	/** build clusters markers to be used at next draw */
	public void renderer(ArrayList<StaticCluster> clusters, ISafeCanvas canvas, MapView mapView){
		for  (StaticCluster cluster:clusters){
			if (cluster.getSize()==1){
				//cluster has only 1 marker => use it as it is:
				cluster.setMarker(cluster.getItem(0));
			} else {
				//only draw 1 Marker at Cluster center, displaying number of Markers contained
				Marker m = buildClusterMarker(cluster, mapView);
				cluster.setMarker(m);
			}
		}
	}
	
	@Override protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		//if zoom has changed and mapView is now stable, rebuild clusters:
		int zoomLevel = mapView.getZoomLevel();
		if (zoomLevel != mLastZoomLevel && !mapView.isAnimating()){
			mClusters = clusterer(mapView);
			renderer(mClusters, canvas, mapView);
			mLastZoomLevel = zoomLevel;
		}
		
		/*
		final Projection pj = mapView.getProjection();
		Drawable dd = new BitmapDrawable(mClusterIcon);
		dd.setBounds(-15, -15, 15, 15);
		*/
		
		for (StaticCluster cluster:mClusters){
			cluster.getMarker().drawSafe(canvas, mapView, shadow);
			
			/*
			if (cluster.getSize()>1){
				GeoPoint p1 = cluster.getPosition();
				GeoPoint p2 = new GeoPoint(p1.getLatitude()+gridSizeY, p1.getLongitude()-gridSizeX);
				Point p2Pixels= pj.toMapPixels(p2, null);
				drawAt(canvas.getSafeCanvas(), dd, p2Pixels.x, p2Pixels.y, false, 0.0f);
			}
			*/
		}
	}

	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		for (StaticCluster cluster:mClusters){
			if (cluster.getMarker().onSingleTapConfirmed(event, mapView))
				return true;
		}
		return false;
	}
	
	@Override public boolean onLongPress(final MotionEvent event, final MapView mapView) {
		for (StaticCluster cluster:mClusters){
			if (cluster.getMarker().onLongPress(event, mapView))
				return true;
		}
		return false;
	}

	@Override public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		for (StaticCluster cluster:mClusters){
			if (cluster.getMarker().onTouchEvent(event, mapView))
				return true;
		}
		return false;
	}
}

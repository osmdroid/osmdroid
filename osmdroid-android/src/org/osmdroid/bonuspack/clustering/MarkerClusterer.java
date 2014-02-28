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
 * Usage: put your markers inside (addItem), and add the MarkerClustered to the map overlays. 
 * Depending on the zoom level, markers will be displayed separately, or grouped. 
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
	protected int mLastZoomLevel = -1; //impossible value
	protected Bitmap mClusterIcon;
	public Paint mTextPaint;
	public float mTextAnchorU, mTextAnchorV;
	protected int mGridSize = 50; //in pixels

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
		mTextAnchorU = Marker.ANCHOR_CENTER;
		mTextAnchorV = Marker.ANCHOR_CENTER;
	}

	public void setIcon(Bitmap icon){
		mClusterIcon = icon;
	}
	
	public void add(Marker marker){
		mItems.add(marker);
		mLastZoomLevel = -1; //force a refresh of clusters
	}
	
	public Marker getItem(int id){
		return mItems.get(id);
	}
	
	public ArrayList<Marker> getItems(){
		return mItems;
	}
	
	public void setGridSize(int gridSize){
		mGridSize = gridSize;
	}
	
	public ArrayList<StaticCluster> clusterer(MapView mapView){
		Rect mScreenRect = mapView.getIntrinsicScreenRect(null);
		int numCellsW = (mScreenRect.right - mScreenRect.left)/mGridSize;
		
		BoundingBoxE6 bb = mapView.getBoundingBox();
		double latSpan = bb.getLatitudeSpanE6()*1E-6;
		double lonSpan = bb.getLongitudeSpanE6()*1E-6;
		//convert grid size from pixels to degrees:
		double gridSizeX = lonSpan * mGridSize / (mScreenRect.right - mScreenRect.left);
		double gridSizeY = latSpan * mGridSize / (mScreenRect.bottom - mScreenRect.top);
		
		ArrayList<StaticCluster> clusters = new ArrayList<StaticCluster>();
		LongSparseArray<StaticCluster> sparseArray = new LongSparseArray<StaticCluster>();
		for (Marker item:mItems){
			int gridX = (int)(item.getPosition().getLongitude() / gridSizeX);
			int gridY = (int)(item.getPosition().getLatitude() / gridSizeY);
			long coord = numCellsW * gridX + gridY;
			StaticCluster cluster = sparseArray.get(coord);
			if (cluster == null) {
                cluster = new StaticCluster(item.getPosition());
                sparseArray.put(coord, cluster);
                clusters.add(cluster);
            }
            cluster.add(item);
		}
		return clusters;
	}
	
	public void renderer(ArrayList<StaticCluster> clusters, ISafeCanvas canvas, MapView mapView){
		for  (StaticCluster cluster:clusters){
			if (cluster.getSize()==1){
				//cluster has only 1 marker => use it as it is:
				cluster.setMarker(cluster.getItem(0));
			} else {
				//only draw 1 Marker at Cluster center, with number of Markers inside
				Marker m = new Marker(mapView);
				m.setPosition(cluster.getPosition());
				m.setInfoWindow(null);
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
				cluster.setMarker(m);
			}
		}
	}
	
	@Override protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		//if zoom has changed and mapView is now stable, rebuild clusters:
		int zoomLevel = mapView.getZoomLevel();
		if (zoomLevel != mLastZoomLevel && !mapView.isAnimating()){
			//Log.d("ZOOM", "zoom="+zoomLevel);
			mClusters = clusterer(mapView);
			renderer(mClusters, canvas, mapView);
			mLastZoomLevel = zoomLevel;
		}
		
		for (StaticCluster cluster:mClusters){
			cluster.getMarker().drawSafe(canvas, mapView, shadow);
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

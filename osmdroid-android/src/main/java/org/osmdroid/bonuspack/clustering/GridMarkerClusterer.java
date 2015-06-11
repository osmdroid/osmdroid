package org.osmdroid.bonuspack.clustering;

import java.util.ArrayList;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LongSparseArray;

/** 
 * Grid-based Clustering algorithm: all markers inside the same cell belong to the same cluster. 
 * The grid size is specified in screen pixels. <br/>
 * 
 * TODO: check what happens with map rotation. <br/>
 * TODO: clustering is not perfectly stable: if you zoom in then out, the mapview lat/lon span may change a little bit,
 * potentially changing the grid positioning 
 * => Considering this issue, this class is deprecated. Use RadiusMarkerClusterer instead. <br>
 * 
 * Largely inspired from (open source) Google Maps Android utility library. 
 * 
 * @see <a href="https://github.com/googlemaps/android-maps-utils">android-maps-utils</a>
 * @author M.Kergall
 *
 */
@Deprecated public class GridMarkerClusterer extends MarkerClusterer {

	protected int mGridSize = 50; //in pixels
	protected Paint mTextPaint;
	
	/** cluster icon anchor */
	public float mAnchorU = Marker.ANCHOR_CENTER, mAnchorV = Marker.ANCHOR_CENTER;
	/** anchor point to draw the number of markers inside the cluster icon */
	public float mTextAnchorU = Marker.ANCHOR_CENTER, mTextAnchorV = Marker.ANCHOR_CENTER;

	public GridMarkerClusterer(Context ctx) {
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
	
	/** Change the size of the clustering grid, in pixels. Default is 50px. */
	public void setGridSize(int gridSize){
		mGridSize = gridSize;
	}

	/** Grid-based clustering algorithm */
	@Override public ArrayList<StaticCluster> clusterer(MapView mapView){
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
	
	/** Build the marker for a cluster. 
	 * Uses the cluster icon, and displays inside the number of markers it contains. <br/>
	 * In the standard Google coordinate system for Marker icons: <br/>
	 * - The cluster icon is anchored at mAnchorU, mAnchorV. <br/>
	 * - The text showing the number of markers is anchored at mTextAnchorU, mTextAnchorV. 
	 * This text is centered horizontally and vertically. */
	@Override public Marker buildClusterMarker(StaticCluster cluster, MapView mapView){
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
				mTextAnchorU*finalIcon.getWidth(), 
				mTextAnchorV*finalIcon.getHeight() - textHeight/2, 
				mTextPaint);
		m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));
		return m;
	}
	
	/** Build clusters markers to be used at next draw */
	@Override public void renderer(ArrayList<StaticCluster> clusters, Canvas canvas, MapView mapView){
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
	
}

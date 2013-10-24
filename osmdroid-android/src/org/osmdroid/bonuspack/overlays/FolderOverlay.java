package org.osmdroid.bonuspack.overlays;

import java.util.AbstractList;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * An overlay which is just a group of other overlays. 
 * 
 * @author M.Kergall
 */
public class FolderOverlay extends Overlay {

	protected OverlayManager mOverlayManager;
	protected String mName, mDescription;
	protected BoundingBoxE6 mBoundingBox;
	
	public FolderOverlay(Context ctx) {
		super(ctx);
		mOverlayManager = new OverlayManager(null);
		mName = "";
		mDescription = "";
		mBoundingBox = null;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setDescription(String description){
		mDescription = description;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public BoundingBoxE6 getBoundingBox(){
		return mBoundingBox;
	}
	
	public AbstractList<Overlay> getItems(){
		return mOverlayManager;
	}
	
	protected void updateBoundingBoxWith(BoundingBoxE6 itemBB){
		if (itemBB != null){
			if (mBoundingBox == null){
				mBoundingBox = new BoundingBoxE6(
						itemBB.getLatNorthE6(), 
						itemBB.getLonEastE6(), 
						itemBB.getLatSouthE6(), 
						itemBB.getLonWestE6());
			} else {
				mBoundingBox = new BoundingBoxE6(
						Math.max(itemBB.getLatNorthE6(), mBoundingBox.getLatNorthE6()), 
						Math.max(itemBB.getLonEastE6(), mBoundingBox.getLonEastE6()),
						Math.min(itemBB.getLatSouthE6(), mBoundingBox.getLatSouthE6()),
						Math.min(itemBB.getLonWestE6(), mBoundingBox.getLonWestE6()));
			}
		}
	}
	
	public boolean add(Overlay item, BoundingBoxE6 itemBB){
		updateBoundingBoxWith(itemBB);
		return mOverlayManager.add(item);
	}
	
	public boolean remove(Overlay item){
		//TODO: update bounding box...
		return mOverlayManager.remove(item);
	}

	@Override protected void draw(Canvas canvas, MapView osm, boolean shadow) {
		if (shadow)
			return;
		mOverlayManager.onDraw(canvas, osm);
	}

	@Override public boolean onSingleTapUp(MotionEvent e, MapView mapView){
		return mOverlayManager.onSingleTapUp(e, mapView);
	}
	
	@Override public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView){
		return mOverlayManager.onSingleTapConfirmed(e, mapView);
	}
	
	@Override public boolean onLongPress(MotionEvent e, MapView mapView){
		return mOverlayManager.onLongPress(e, mapView);
	}
	
	//TODO: implement other events
}

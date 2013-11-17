package org.osmdroid.bonuspack.overlays;

import java.util.AbstractList;
import java.util.HashMap;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import android.annotation.SuppressLint;
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
	HashMap<Overlay, BoundingBoxE6> mItemsBoundingBoxes; //bounding box of each item
	protected String mName, mDescription;
	protected BoundingBoxE6 mBoundingBox;
	
	public FolderOverlay(Context ctx) {
		super(ctx);
		mOverlayManager = new OverlayManager(null);
		mName = "";
		mDescription = "";
		mBoundingBox = null;
		mItemsBoundingBoxes = new HashMap<Overlay, BoundingBoxE6>();
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
	
	/**
	 * @return the bounding box enclosing all components of the folder. Null if empty. 
	 */
	public BoundingBoxE6 getBoundingBox(){
		return mBoundingBox;
	}
	
	/**
	 * @return the list of components of this folder. 
	 * Doesn't provide a copy, but the actual list. 
	 */
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
		mItemsBoundingBoxes.put(item, itemBB);
		return mOverlayManager.add(item);
	}
	
	public boolean remove(Overlay item){
		BoundingBoxE6 bb = mItemsBoundingBoxes.remove(item);
		if (bb != null){
			//refresh global bounding box from scratch:
			mBoundingBox = null;
			for (BoundingBoxE6 itemBB : mItemsBoundingBoxes.values()) {
				updateBoundingBoxWith(itemBB);
			}
		}
		return mOverlayManager.remove(item);
	}

	@SuppressLint("WrongCall")
	@Override protected void draw(Canvas canvas, MapView osm, boolean shadow) {
		if (shadow)
			return;
		mOverlayManager.onDraw(canvas, osm);
	}

	@Override public boolean onSingleTapUp(MotionEvent e, MapView mapView){
		if (isEnabled())
			return mOverlayManager.onSingleTapUp(e, mapView);
		else 
			return false;
	}
	
	@Override public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView){
		if (isEnabled())
			return mOverlayManager.onSingleTapConfirmed(e, mapView);
		else 
			return false;
	}
	
	@Override public boolean onLongPress(MotionEvent e, MapView mapView){
		if (isEnabled())
			return mOverlayManager.onLongPress(e, mapView);
		else 
			return false;
	}
	//TODO: implement other events
		
}

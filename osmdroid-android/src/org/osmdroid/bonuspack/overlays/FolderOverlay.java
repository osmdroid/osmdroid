package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import android.content.Context;
import android.graphics.Canvas;

/**
 * An overlay which is just a group of other overlays. 
 * 
 * Unfortunately, no simple and clean way to implement it, as : 
 * - Overlay.draw is protected. 
 * - And we cannot use a local OverlayManager, because OverlayManager requires a tilesOverlay in osmdroid 3.0.10. 
 * Waiting next version, with inclusion of #486 fix. 
 * 
 * @author M.Kergall
 */
public class FolderOverlay extends Overlay {

	protected List<Overlay> mItems;
	protected OverlayManager mOverlayManager;
	protected String mName;
	protected BoundingBoxE6 mBoundingBox;
	
	public FolderOverlay(Context ctx, MapView mapView) {
		super(ctx);
		mOverlayManager = mapView.getOverlayManager();
		mItems = new ArrayList<Overlay>();
		mName = "";
		mBoundingBox = null;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public BoundingBoxE6 getBoundingBox(){
		return mBoundingBox;
	}
	
	/**
	 * properly removes itself and all its components from the map overlays. 
	 */
	public void removeFromMap(){
		for (Overlay item:mItems){
			mOverlayManager.remove(item);
			if (item.getClass()==FolderOverlay.class){
				((FolderOverlay)item).removeFromMap();
			}
		}
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
		if (item.getClass()!=FolderOverlay.class){
			mOverlayManager.add(item);
		}
		updateBoundingBoxWith(itemBB);
		return mItems.add(item);
	}
	
	public boolean remove(Overlay item){
		if (item.getClass()!=FolderOverlay.class){
			mOverlayManager.remove(item);
		}
		//TODO: update bounding box...
		return mItems.remove(item);
	}

	@Override protected void draw(Canvas canvas, MapView osm, boolean shadow) {
		//Nothing to do, draw of items is handled by the mOverlayManager. 
		/*
		for (DrawableOverlay item:items){
			if (item.isEnabled())
				item.draw((Canvas)canvas, osm, shadow);
		}
		*/
	}
}

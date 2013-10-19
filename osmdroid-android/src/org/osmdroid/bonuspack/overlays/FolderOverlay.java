package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;
import java.util.List;

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
	
	public FolderOverlay(Context ctx, MapView mapView) {
		super(ctx);
		mOverlayManager = mapView.getOverlayManager();
		mItems = new ArrayList<Overlay>();
		mName = "";
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public void removeFromOverlayManager(){
		for (Overlay item:mItems){
			mOverlayManager.remove(item);
			if (item.getClass()==FolderOverlay.class){
				((FolderOverlay)item).removeFromOverlayManager();
			}
		}
	}
	
	public boolean add(Overlay item){
		mOverlayManager.add(item);
		//TODO: add subfolders of the item, if FolderOverlay
		return mItems.add(item);
	}
	
	public boolean remove(Overlay item){
		mOverlayManager.remove(item);
		//TODO: remove subfolders of the item, if FolderOverlay
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

package org.osmdroid.bonuspack.overlays;

import java.util.AbstractList;
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
	protected String mName, mDescription;
	
	public FolderOverlay(Context ctx) {
		super(ctx);
		mOverlayManager = new OverlayManager(null);
		mName = "";
		mDescription = "";
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
	 * @return the list of components of this folder. 
	 * Doesn't provide a copy, but the actual list. 
	 */
	public AbstractList<Overlay> getItems(){
		return mOverlayManager;
	}
	
	public boolean add(Overlay item){
		return mOverlayManager.add(item);
	}
	
	public boolean remove(Overlay item){
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

	@Override public boolean onTouchEvent(MotionEvent e, MapView mapView){
		if (isEnabled())
			return mOverlayManager.onTouchEvent(e, mapView);
		else 
			return false;
	}
	
	//TODO: implement other events... 
	
	/**
	 * Close all opened InfoWindows of overlays it contains. 
	 * This only operates on overlays that inherit from OverlayWithIW. 
	 */
	public void closeAllInfoWindows(){
		for (Overlay overlay:mOverlayManager){
			if (overlay instanceof FolderOverlay){
				((FolderOverlay)overlay).closeAllInfoWindows();
			} else if (overlay instanceof OverlayWithIW){
				((OverlayWithIW)overlay).closeInfoWindow();
			}
		}
	}

}

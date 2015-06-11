package org.osmdroid.bonuspack.overlays;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** View that can be displayed on an OSMDroid map, associated to a GeoPoint. 
 * Typical usage: cartoon-like bubbles displayed when clicking an overlay item. 
 * It mimics the InfoWindow class of Google Maps JavaScript API V3. 
 * Main differences are: 
 * <ul>
 * <li>Structure and content of the view is let to the responsibility of the caller. </li>
 * <li>The same InfoWindow can be associated to many items. </li>
 * </ul>
 * 
 * This is an abstract class. 
 * @see MarkerInfoWindow
 * @author M.Kergall
 */
public abstract class InfoWindow {

	protected View mView;
	protected boolean mIsVisible;
	protected MapView mMapView;
	
	/**
	 * @param layoutResId	the id of the view resource. 
	 * @param mapView	the mapview on which is hooked the view
	 */
	public InfoWindow(int layoutResId, MapView mapView) {
		mMapView = mapView;
		mIsVisible = false;
		ViewGroup parent=(ViewGroup)mapView.getParent();
		Context context = mapView.getContext();
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(layoutResId, parent, false);
		mView.setTag(this);
	}

	/**
	 * Returns the Android view. This allows to set its content. 
	 * @return the Android view
	 */
	public View getView() {
		return(mView);
	}

	/**
	 * open the InfoWindow at the specified GeoPosition + offset. 
	 * If it was already opened, close it before reopening. 
	 * @param object the graphical object on which is hooked the view
	 * @param position to place the window on the map
	 * @param offsetX (&offsetY) the offset of the view to the position, in pixels. 
	 * This allows to offset the view from the object position. 
	 */
	public void open(Object object, GeoPoint position, int offsetX, int offsetY) {
		close(); //if it was already opened
		onOpen(object);
		MapView.LayoutParams lp = new MapView.LayoutParams(
				MapView.LayoutParams.WRAP_CONTENT,
				MapView.LayoutParams.WRAP_CONTENT,
				position, MapView.LayoutParams.BOTTOM_CENTER, 
				offsetX, offsetY);
		mMapView.addView(mView, lp);
		mIsVisible = true;
	}
    
	public void close() {
		if (mIsVisible) {
			mIsVisible = false;
			((ViewGroup)mView.getParent()).removeView(mView);
			onClose();
		}
	}
	
	public boolean isOpen(){
		return mIsVisible;
	}
	
	/** close all InfoWindows currently opened on this MapView */
	static public void closeAllInfoWindowsOn(MapView mapView){
		ArrayList<InfoWindow> opened = getOpenedInfoWindowsOn(mapView);
		for (InfoWindow infoWindow:opened){
			infoWindow.close();
		}
	}
	
	/** return all InfoWindows currently opened on this MapView */
	static public ArrayList<InfoWindow> getOpenedInfoWindowsOn(MapView mapView){
		int count = mapView.getChildCount();
		ArrayList<InfoWindow> opened = new ArrayList<InfoWindow>(count);
		for (int i = 0; i < count; i++) {
			final View child = mapView.getChildAt(i);
			Object tag = child.getTag();
			if (tag != null && tag instanceof InfoWindow){
				InfoWindow infoWindow = (InfoWindow)tag;
				opened.add(infoWindow);
			}
		}
		return opened;
	}
	
	//Abstract methods to implement in sub-classes:
	public abstract void onOpen(Object item);
	public abstract void onClose();
	
}

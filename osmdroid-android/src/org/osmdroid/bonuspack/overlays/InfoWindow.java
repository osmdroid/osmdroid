package org.osmdroid.bonuspack.overlays;

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
 * <li>There is no "Close" button. The window is closed when clicking on it. </li>
 * <li>The same InfoWindow can be associated to many items. </li>
 * </ul>
 * Known issues:
 * <ul>
 * <li>It disappears when zooming in/out. </li>
 * <li>The window is displayed above the marker. </li>
 * </ul>
 * 
 * @author M.Kergall
 */
public class InfoWindow {

	protected View mView;
	protected boolean mIsVisible = false;
	protected MapView mMapView;
	
	/**
	 * @param layoutResId	the id of the view resource. 
	 * @param mapView	the mapview on which is hooked the view
	 */
	public InfoWindow(int layoutResId, MapView mapView) {
		mMapView = mapView;
		mIsVisible = false;
		ViewGroup parent=(ViewGroup)mapView.getParent();
		Context ctx = mapView.getContext();
		LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		/*
		if (layoutResId == 0) //default layout
			layoutResId = R.layout.bonuspack_bubble; => KO: layout not part of the jar...
		*/
		mView = inflater.inflate(layoutResId, parent, false);
		mView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
			}
		});
	}

	/**
	 * Returns the Android view. This allows to set its content. 
	 * @return the Android view
	 */
	public View getView() {
		return(mView);
	}
    
	/**
	 * open the window at the specified position. 
	 * @param position the geopoint on which is hooked the view
	 * @param offsetX (&offsetY) the offset of the view to the position, in pixels. 
	 * This allows to offset the view from the marker position. 
	 */
	public void open(GeoPoint position, int offsetX, int offsetY) {
		MapView.LayoutParams lp=new MapView.LayoutParams(
				MapView.LayoutParams.WRAP_CONTENT,
				MapView.LayoutParams.WRAP_CONTENT,
				position, MapView.LayoutParams.BOTTOM_CENTER, 
				offsetX, offsetY);
		close();
		mMapView.addView(mView, lp);
		mIsVisible = true;
	}
    
	public void close() {
		if (mIsVisible) {
			mIsVisible = false;
			((ViewGroup)mView.getParent()).removeView(mView);
		}
	}
	
	public boolean isOpen(){
		return mIsVisible;
	}
	
	public void setPosition(GeoPoint p, int offsetX, int offsetY){
		if (mIsVisible){
			open(p, offsetX, offsetY);
		}
	}
}

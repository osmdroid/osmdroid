package com.osmbonuspackdemo;

import java.util.ArrayList;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;


/* Implementation that doesn't work...  
public class MyItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
	protected Context mContext;
	
	public MyOwnItemizedOverlay(Drawable defaultMarker, Context context) {
		 super(new ArrayList<OverlayItem>(), 
				defaultMarker, 
				null, 
				new DefaultResourceProxyImpl(context));
	}
	
	@Override public boolean onSingleTapUpHelper(int i, OverlayItem item, MapView mapView) {
		Toast.makeText(mContext, "Item " + i + " has been tapped!", Toast.LENGTH_SHORT).show();
		return true;
	}

}
*/

public class MyItemizedOverlay {
	protected ItemizedIconOverlay<OverlayItem> mOverlay;
	protected Context mContext;
	protected Drawable mMarker;

	public MyItemizedOverlay(Drawable marker, Context context) {
		mContext = context;
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		ResourceProxy resourceProxy = (ResourceProxy) new DefaultResourceProxyImpl(mContext);
		mMarker = marker;

		mOverlay = new ItemizedIconOverlay<OverlayItem>(
				items, mMarker, 
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			@Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return onSingleTapUpHelper(index, item);
			}
	
			@Override public boolean onItemLongPress(final int index, final OverlayItem item) {
				return true;
			}
		}, resourceProxy);

	}
	
	public boolean onSingleTapUpHelper(int i, OverlayItem item) {
		//Toast.makeText(mContext, "Item " + i + " has been tapped!", Toast.LENGTH_SHORT).show();
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}
	
	public void addItem(OverlayItem item){
		mOverlay.addItem(item);
	}
	
	public ItemizedIconOverlay<OverlayItem> getOverlay(){
		return mOverlay;
	}
}

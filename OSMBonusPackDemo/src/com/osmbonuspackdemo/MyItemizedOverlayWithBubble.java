package com.osmbonuspackdemo;

import java.util.ArrayList;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.InfoWindow;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MyItemizedOverlayWithBubble {
	protected ItemizedIconOverlay<OverlayItem> mOverlay;
	protected Context mContext;
	protected MapView mView;
	protected InfoWindow mPanel;
	protected OverlayItem itemWithPanel; //the item currently showing the panel. Null if none. 
	
	public MyItemizedOverlayWithBubble(MapView view, Context context) {
		mContext = context;
		mView = view;
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		ResourceProxy resourceProxy = (ResourceProxy) new DefaultResourceProxyImpl(mContext);

		mOverlay = new ItemizedIconOverlay<OverlayItem>(
				items, 
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			@Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return onSingleTapUpHelper(index);
			}
	
			@Override public boolean onItemLongPress(final int index, final OverlayItem item) {
				return false;
			}
		}, resourceProxy);

		mPanel = new InfoWindow(R.layout.bonuspack_bubble, mView);
		itemWithPanel = null;
	}
	
	public boolean onSingleTapUpHelper(int i) {
		ExtendedOverlayItem eItem = (ExtendedOverlayItem)(mOverlay.getItem(i)); 
		itemWithPanel = eItem;
		GeoPoint position = eItem.getPoint();
		//update the content of the popup panel, based on the item tapped:
		View view = mPanel.getView();
		((TextView)view.findViewById(R.id.title)).setText(eItem.getTitle());
		((TextView)view.findViewById(R.id.description)).setText(eItem.getDescription());
		
		//TODO: handle mSubDescription, hidding or showing the text view
		
		ImageView imageView = (ImageView)view.findViewById(R.id.image);
		Drawable image = eItem.getImage();
		if (image != null){
			imageView.setBackgroundDrawable(image);
			imageView.setVisibility(View.VISIBLE);
		} else
			imageView.setVisibility(View.GONE);
		
		int offsetY = -20;
		Drawable marker = eItem.getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK);
		if (marker != null)
			offsetY = -marker.getIntrinsicHeight()*3/4;
		mPanel.open(position, 0, offsetY);
		mView.getController().animateTo(position);
		return true;
	}
	
	public void addItem(ExtendedOverlayItem item){
		mOverlay.addItem(item);
	}

	public void removeItem(ExtendedOverlayItem item){
		mOverlay.removeItem(item);
		if (itemWithPanel == item){
			mPanel.close();
			itemWithPanel = null;
		}
	}
	
	public void removeAllItems(){
		mOverlay.removeAllItems();
		mPanel.close();
		itemWithPanel = null;
	}
	
	public ItemizedIconOverlay<OverlayItem> getOverlay(){
		return mOverlay;
	}
}

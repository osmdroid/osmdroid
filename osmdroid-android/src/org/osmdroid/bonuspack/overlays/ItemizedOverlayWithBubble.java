package org.osmdroid.bonuspack.overlays;

import java.util.List;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;

/**
 * An itemized overlay with an InfoWindow or "bubble" which opens 
 * when the user taps on an overlay item, and displays item attributes. <br>
 * Items must be ExtendedOverlayItem. <br>
 * 
 * 
 * @see ExtendedOverlayItem
 * @see InfoWindow
 * 
 * @author M.Kergall
 */
public class ItemizedOverlayWithBubble<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {
	protected List<Item> mItemsList;
	protected InfoWindow mBubble; //only one for all items of this overlay => one at a time
	protected OverlayItem mItemWithBubble; //the item currently showing the bubble. Null if none. 
	
	public ItemizedOverlayWithBubble(final Context context, final List<Item> aList, 
			final MapView mapView, int layoutResId) {
		super(context, aList, new OnItemGestureListener<Item>() {
            @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
            }
            @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
            }
    	} );
		mItemsList = aList;
		mBubble = new InfoWindow(layoutResId, mapView);
		mItemWithBubble = null;
	}

	/**
	 * Opens the bubble on the item. 
	 * For each ItemizedOverlay, only one bubble is opened at a time. 
	 * If you want more bubbles opened simultaneously, use many ItemizedOverlays. 
	 * 
	 * @param index of the overlay item to show
	 * @param mapView
	 */
	public void showBubbleOnItem(int index, MapView mapView) {
		ExtendedOverlayItem eItem = (ExtendedOverlayItem)(getItem(index)); 
		mItemWithBubble = eItem;
		if (eItem != null)
			eItem.showBubble(mBubble, mapView);
	}
	
	@Override protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
		showBubbleOnItem(index, mapView);
		return true;
	}
	
	/** @return the item currenty showing the bubble, or null if none.  */
	public OverlayItem getBubbledItem(){
		if (mBubble.isOpen())
			return mItemWithBubble;
		else
			return null;
	}
	
	/** @return the index of the item currenty showing the bubble, or -1 if none.  */
	public int getBubbledItemId(){
		OverlayItem item = getBubbledItem();
		if (item == null)
			return -1;
		else
			return mItemsList.indexOf(item);
	}
	
	@Override public boolean removeItem(Item item){
		boolean result = super.removeItem(item);
		if (mItemWithBubble == item){
			mBubble.close();
			mItemWithBubble = null;
		}
		return result;
	}
	
	@Override public void removeAllItems(){
		super.removeAllItems();
		mBubble.close();
		mItemWithBubble = null;
	}

}

package org.osmdroid.bonuspack.overlays;

import java.util.List;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

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

	static int layoutResId = 0;
	
	public ItemizedOverlayWithBubble(final Context context, final List<Item> aList, 
			final MapView mapView, final InfoWindow bubble) {
		super(context, aList, new OnItemGestureListener<Item>() {
            @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
            }
            @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
            }
    	} );
		mItemsList = aList;
		if (bubble != null){
			mBubble = bubble;
		} else {
			//build default bubble:
			String packageName = context.getPackageName();
			if (layoutResId == 0){
				layoutResId = context.getResources().getIdentifier("layout/bonuspack_bubble", null, packageName);
				if (layoutResId == 0)
					Log.e(BonusPackHelper.LOG_TAG, "ItemizedOverlayWithBubble: layout/bonuspack_bubble not found in "+packageName);
			}
			mBubble = new DefaultInfoWindow(layoutResId, mapView);
		}
		mItemWithBubble = null;
	}

	public ItemizedOverlayWithBubble(final Context context, final List<Item> aList, 
			final MapView mapView) {
		this(context, aList, mapView, null);
	}
	
	/**
	 * Opens the bubble on the item. 
	 * For each ItemizedOverlay, only one bubble is opened at a time. 
	 * If you want more bubbles opened simultaneously, use many ItemizedOverlays. 
	 * 
	 * @param index of the overlay item to show
	 * @param mapView
	 */
	public void showBubbleOnItem(final int index, final MapView mapView) {
		ExtendedOverlayItem eItem = (ExtendedOverlayItem)(getItem(index)); 
		mItemWithBubble = eItem;
		if (eItem != null){
			eItem.showBubble(mBubble, mapView);
			//setFocus((Item)eItem);
		}
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
	
	@Override public boolean removeItem(final Item item){
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

	//fixing drawing focused item on top in ItemizedOverlay (osmdroid issue 354):
	@Override public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		if (shadow) {
		        return;
		}
		final Projection pj = mapView.getProjection();
		final int size = mItemsList.size() - 1;
		final Point mCurScreenCoords = new Point();
		
		/* Draw in backward cycle, so the items with the least index are on the front. */
		for (int i = size; i >= 0; i--) {
	        final Item item = getItem(i);
			if (item != mItemWithBubble){
		        pj.toMapPixels(item.mGeoPoint, mCurScreenCoords);
		        onDrawItem(canvas, item, mCurScreenCoords);
			}
		}
		//draw focused item last:
		if (mItemWithBubble != null){
	        pj.toMapPixels(mItemWithBubble.mGeoPoint, mCurScreenCoords);
	        onDrawItem(canvas, (Item)mItemWithBubble, mCurScreenCoords);
		}
    }
}

package org.osmdroid.bonuspack.overlays;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An itemized overlay with an InfoWindow or "bubble" which opens 
 * when the user taps on an overlay item, and displays item attributes. <br>
 * Items must be ExtendedOverlayItem. 
 * @see ExtendedOverlayItem
 * @see InfoWindow
 * @author M.Kergall
 */
public class ItemizedOverlayWithBubble<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {
	protected InfoWindow mBubble; //only one for all items of this overlay => one at a time
	protected OverlayItem mItemWithBubble; //the item currently showing the bubble. Null if none. 
	final int mTitleId, mDescriptionId, mSubDescriptionId, mImageId;
	
	public ItemizedOverlayWithBubble(final Context context, final List<Item> aList, 
			final MapView mapView) {
		super(context, aList, new OnItemGestureListener<Item>() {
            @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
            }
            @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
            }
    	} );
		String packageName = context.getClass().getPackage().getName();
		int layoutId = context.getResources().getIdentifier( "layout/bonuspack_bubble" , null, packageName);
		mTitleId = context.getResources().getIdentifier( "id/bubble_title" , null, packageName);
		mDescriptionId = context.getResources().getIdentifier( "id/bubble_description" , null, packageName);
		mSubDescriptionId = context.getResources().getIdentifier( "id/bubble_subdescription" , null, packageName);
		mImageId = context.getResources().getIdentifier( "id/bubble_image" , null, packageName);
		mBubble = new InfoWindow(layoutId, mapView);
		mItemWithBubble = null;
	}
	
	public void showBubbleOnItem(int index, MapView mapView) {
		ExtendedOverlayItem eItem = (ExtendedOverlayItem)(getItem(index)); 
		mItemWithBubble = eItem;
		GeoPoint position = eItem.getPoint();
		//update the content of the bubble, based on the item tapped:
		View view = mBubble.getView();
		((TextView)view.findViewById(mTitleId /*R.id.title*/)).setText(eItem.getTitle());
		((TextView)view.findViewById(mDescriptionId /*R.id.description*/)).setText(eItem.getDescription());
		
		//handle mSubDescription, hidding or showing the text view:
		TextView subDescText = (TextView)view.findViewById(mSubDescriptionId);
		String subDesc = eItem.getSubDescription();
		if (subDesc != null && !("".equals(subDesc))){
			subDescText.setText(subDesc);
			subDescText.setVisibility(View.VISIBLE);
		} else {
			subDescText.setVisibility(View.GONE);
		}
		
		ImageView imageView = (ImageView)view.findViewById(mImageId /*R.id.image*/);
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
		mBubble.open(position, 0, offsetY);
		mapView.getController().animateTo(position);
	}
	
	@Override protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
		showBubbleOnItem(index, mapView);
		return true;
	}
	
	/** @return the item currenty showing the bubble, or null if none.  */
	public OverlayItem getBubbledItem(){
		//TODO: if user taps the bubble to close it, mItemWithBubble is not set to null... 
		return mItemWithBubble;
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

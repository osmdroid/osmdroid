package org.osmdroid.bonuspack.overlays;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * An OverlayItem to use in ItemizedOverlayWithBubble<br>
 * - more complete: can contain an image and a sub-description that will be displayed in the bubble, <br>
 * - and flexible: attributes are modifiable<br>
 * 
 * Known Issues:<br>
 * - Bubble offset is not perfect on h&xhdpi resolutions, due to an osmdroid issue on marker drawing<br>
 * - Bubble offset is at 0 when using the default marker => set the marker on each item!<br>
 * 
 * @see ItemizedOverlayWithBubble
 * @author M.Kergall
 */
public class ExtendedOverlayItem extends OverlayItem {

	private String mTitle, mDescription; // now, they are modifiable
	private String mSubDescription; //a third field that can be displayed in the infowindow, on a third line
	private Drawable mImage; //that will be shown in the infowindow. 
	//private GeoPoint mGeoPoint //unfortunately, this is not so simple...
	private Object mRelatedObject; //reference to an object (of any kind) linked to this item. 
	
	public ExtendedOverlayItem(String aTitle, String aDescription,
			GeoPoint aGeoPoint, Context context) {
		super(aTitle, aDescription, aGeoPoint);
		mTitle = aTitle;
		mDescription = aDescription;
		mSubDescription = null;
		mImage = null;
		mRelatedObject = null;
	}

	public void setTitle(String aTitle){
		mTitle = aTitle;
	}
	
	public void setDescription(String aDescription){
		mDescription = aDescription;
	}
	
	public void setSubDescription(String aSubDescription){
		mSubDescription = aSubDescription;
	}
	
	public void setImage(Drawable anImage){
		mImage = anImage;
	}

	public void setRelatedObject(Object o){
		mRelatedObject = o;
	}
	
	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getSubDescription() {
		return mSubDescription;
	}

	public Drawable getImage() {
		return mImage;
	}

	public Object getRelatedObject(){
		return mRelatedObject;
	}
	
	/** 
	 * From a HotspotPlace and drawable dimensions (width, height), return the hotspot position. 
	 * Could be a public method of HotspotPlace or OverlayItem... 
	 */
	public Point getHotspot(HotspotPlace place, int w, int h){
		Point hp = new Point();
		if (place == null)
			place = HotspotPlace.BOTTOM_CENTER; //use same default than in osmdroid. 
		switch (place){
		case NONE : 
			hp.set(0, 0);
			break;
		case BOTTOM_CENTER:
			hp.set(w/2, 0);
			break;
		case LOWER_LEFT_CORNER: 
			hp.set(0, 0);
			break;
		case LOWER_RIGHT_CORNER:
			hp.set(w, 0);
			break;
		case CENTER:
			hp.set(w/2, -h/2);
			break;
		case LEFT_CENTER:
			hp.set(0, -h/2);
			break;
		case RIGHT_CENTER:
			hp.set(w, -h/2);
			break;
		case TOP_CENTER:
			hp.set(w/2, -h);
			break;
		case UPPER_LEFT_CORNER:
			hp.set(0, -h);
			break;
		case UPPER_RIGHT_CORNER:
			hp.set(w, -h);
			break;
		}
		return hp;
	}
	
	/**
	 * Populates this bubble with all item info:
	 * <ul>title and description in any case, </ul>
	 * <ul>image and sub-description if any.</ul> 
	 * and centers the map view on the item if panIntoView is true. <br>
	 */
	public void showBubble(InfoWindow bubble, MapView mapView, boolean panIntoView){
		//offset the bubble to be top-centered on the marker:
		Drawable marker = getMarker(0 /*OverlayItem.ITEM_STATE_FOCUSED_MASK*/);
		int markerWidth = 0, markerHeight = 0;
		if (marker != null){
			markerWidth = marker.getIntrinsicWidth(); 
			markerHeight = marker.getIntrinsicHeight();
		} //else... we don't have the default marker size => don't user default markers!!!
		Point markerH = getHotspot(getMarkerHotspot(), markerWidth, markerHeight);
		Point bubbleH = getHotspot(HotspotPlace.TOP_CENTER, markerWidth, markerHeight);
		bubbleH.offset(-markerH.x, -markerH.y);
		
		bubble.open(this, this.getPoint(), bubbleH.x, bubbleH.y);
		if (panIntoView)
			mapView.getController().animateTo(getPoint());
	}
}

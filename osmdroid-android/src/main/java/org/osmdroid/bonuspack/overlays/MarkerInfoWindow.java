package org.osmdroid.bonuspack.overlays;

import org.osmdroid.views.MapView;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * Default implementation of InfoWindow for a Marker. 
 * It handles a text and a description. 
 * It also handles optionally a sub-description and an image. 
 * Description and sub-description interpret HTML tags (in the limits of the Html.fromHtml(String) API). 
 * Clicking on the bubble will close it. 
 * 
 * @author M.Kergall
 */
public class MarkerInfoWindow extends BasicInfoWindow {

	protected Marker mMarkerRef; //reference to the Marker on which it is opened. Null if none. 
	
	public MarkerInfoWindow(int layoutResId, MapView mapView) {
		super(layoutResId, mapView);
		//mMarkerRef = null;
	}
	
	@Override public void onOpen(Object item) {
		super.onOpen(item);
		
		mMarkerRef = (Marker)item;
		
		//handle image
		ImageView imageView = (ImageView)mView.findViewById(mImageId /*R.id.image*/);
		Drawable image = mMarkerRef.getImage();
		if (image != null){
			imageView.setImageDrawable(image); //or setBackgroundDrawable(image)?
			imageView.setVisibility(View.VISIBLE);
		} else
			imageView.setVisibility(View.GONE);
	}

	@Override public void onClose() {
		super.onClose();
		mMarkerRef = null;
		//by default, do nothing else
	}
	
}

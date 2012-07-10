package com.osmbonuspackdemo;

import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.MapView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * An example of a customized InfoWindow. 
 * We inherit from DefaultInfoWindow as it already provides most of what we want. 
 * And we just add support for a "more info" button. 
 * 
 * @author M.Kergall
 */
public class CustomInfoWindow extends DefaultInfoWindow {
	
	private ExtendedOverlayItem mCurrentItem;
	
	public CustomInfoWindow(MapView mapView) {
		super(R.layout.bonuspack_bubble_black, mapView);
		
		Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
			//yes, bonuspack_bubble layouts have a "more info" button. 
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "I confirm: " + mCurrentItem.getDescription(), Toast.LENGTH_LONG).show();
				
				//If you had set a related object on this item, you can get it here:
				Object relatedObject = mCurrentItem.getRelatedObject();
				if (relatedObject != null){
					//...
				}
			}
		});
	}

	@Override public void onOpen(ExtendedOverlayItem item){
		super.onOpen(item);
		mCurrentItem = item;
		mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
	}
}

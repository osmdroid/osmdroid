package org.osmdroid.bonuspack.overlays;

import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.views.MapView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Default implementation of InfoWindow for a Marker. 
 * It handles a text and a description. 
 * It also handles optionally a sub-description and an image. 
 * Description and sub-description interpret HTML tags (in the limits of the Html.fromHtml(String) API). 
 * Clicking on the bubble will close it. 
 * 
 * @author M.Kergall
 */
public class MarkerInfoWindow extends InfoWindow {

	static int mTitleId=BonusPackHelper.UNDEFINED_RES_ID, 
			mDescriptionId=BonusPackHelper.UNDEFINED_RES_ID, 
			mSubDescriptionId=BonusPackHelper.UNDEFINED_RES_ID, 
			mImageId=BonusPackHelper.UNDEFINED_RES_ID; //resource ids

	protected Marker mMarkerRef; //reference to the Marker on which it is opened. Null if none. 
	
	private static void setResIds(Context context){
		String packageName = context.getPackageName(); //get application package name
		mTitleId = context.getResources().getIdentifier("id/bubble_title", null, packageName);
		mDescriptionId = context.getResources().getIdentifier("id/bubble_description", null, packageName);
		mSubDescriptionId = context.getResources().getIdentifier("id/bubble_subdescription", null, packageName);
		mImageId = context.getResources().getIdentifier("id/bubble_image", null, packageName);
		if (mTitleId == BonusPackHelper.UNDEFINED_RES_ID || mDescriptionId == BonusPackHelper.UNDEFINED_RES_ID 
				|| mSubDescriptionId == BonusPackHelper.UNDEFINED_RES_ID || mImageId == BonusPackHelper.UNDEFINED_RES_ID) {
			Log.e(BonusPackHelper.LOG_TAG, "MarkerInfoWindow: unable to get res ids in "+packageName);
		}
	}
	
	public MarkerInfoWindow(int layoutResId, MapView mapView) {
		super(layoutResId, mapView);
		
		if (mTitleId == BonusPackHelper.UNDEFINED_RES_ID)
			setResIds(mapView.getContext());
		
		//default behavior: close it when clicking on the bubble:
		mView.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_UP)
					close();
				return true;
			}
		});
	}
	
	@Override public void onOpen(Object item) {
		mMarkerRef = (Marker)item;
		String title = mMarkerRef.getTitle();
		if (title == null)
			title = "";
		((TextView)mView.findViewById(mTitleId /*R.id.title*/)).setText(title);
		
		String snippet = mMarkerRef.getSnippet();
		if (snippet == null)
			snippet = "";
		Spanned snippetHtml = Html.fromHtml(snippet);
		((TextView)mView.findViewById(mDescriptionId /*R.id.description*/)).setText(snippetHtml);
		
		//handle sub-description, hidding or showing the text view:
		TextView subDescText = (TextView)mView.findViewById(mSubDescriptionId);
		String subDesc = mMarkerRef.getSubDescription();
		if (subDesc != null && !("".equals(subDesc))){
			subDescText.setText(Html.fromHtml(subDesc));
			subDescText.setVisibility(View.VISIBLE);
		} else {
			subDescText.setVisibility(View.GONE);
		}

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
		mMarkerRef = null;
		//by default, do nothing else
	}
	
}

package org.osmdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 * an example resource proxy that overrides the person icon
 * @author alex
 */
public class CustomResourceProxy extends DefaultResourceProxyImpl {

     private final Context mContext;
     public CustomResourceProxy(Context pContext) {
          super(pContext);
		mContext = pContext;
     }
     
     @Override
	public Bitmap getBitmap(final bitmap pResId) {
		switch (pResId){
               case person:
                    //your image goes here!!!
                    return BitmapFactory.decodeResource(mContext.getResources(),org.osmdroid.R.drawable.sfgpuci);
                 
          }
          return super.getBitmap(pResId);
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		switch (pResId){
               case person:
                    return mContext.getResources().getDrawable(org.osmdroid.R.drawable.sfgpuci);
          }
          return super.getDrawable(pResId);
	}
     
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 *
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
                    return BitmapFactory.decodeResource(mContext.getResources(),org.osmdroid.example.R.drawable.sfgpuci);
                 
          }
          return super.getBitmap(pResId);
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		switch (pResId){
               case person:
                    return mContext.getResources().getDrawable(org.osmdroid.example.R.drawable.sfgpuci);
          }
          return super.getDrawable(pResId);
	}
     
}

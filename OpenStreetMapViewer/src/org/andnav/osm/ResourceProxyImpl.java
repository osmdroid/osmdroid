package org.andnav.osm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class ResourceProxyImpl implements ResourceProxy {

	private final Context mContext;
	
	public ResourceProxyImpl(final Context pContext) {
		mContext = pContext;
	}

	@Override
	public String getString(int pResId) {
		switch(pResId) {
		case string.osmarender : return mContext.getString(R.string.osmarender);
		case string.mapnik : return mContext.getString(R.string.mapnik);
		case string.cyclemap : return mContext.getString(R.string.cyclemap);
		case string.openareal_sat : return mContext.getString(R.string.openareal_sat);
		case string.base : return mContext.getString(R.string.base);
		case string.topo : return mContext.getString(R.string.topo);
		case string.hills : return mContext.getString(R.string.hills);
		case string.cloudmade_small : return mContext.getString(R.string.cloudmade_small);
		case string.cloudmade_standard : return mContext.getString(R.string.cloudmade_standard);
		case string.unknown : return mContext.getString(R.string.unknown);
		default : throw new IllegalArgumentException();
		}
	}

	@Override
	public Bitmap getBitmap(int pResId) {
		switch(pResId) {
		case bitmap.zoom_in : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.zoom_in);
		case bitmap.zoom_out : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.zoom_out);
		case bitmap.person : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person);
		case bitmap.direction_arrow : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.direction_arrow);
		case bitmap.previous : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.previous);
		case bitmap.next : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.next);
		case bitmap.center : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.center);
		case bitmap.navto_small : return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.navto_small);
		default : throw new IllegalArgumentException();
		}
	}

	@Override
	public Drawable getDrawable(int pResId) {
		switch(pResId) {
		case drawable.marker_default : return mContext.getResources().getDrawable(R.drawable.marker_default);
		case drawable.marker_default_focused_base : return mContext.getResources().getDrawable(R.drawable.marker_default_focused_base);
		default : throw new IllegalArgumentException();
		}
	}
}

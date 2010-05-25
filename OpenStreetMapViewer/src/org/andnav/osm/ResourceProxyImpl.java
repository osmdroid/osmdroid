package org.andnav.osm;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class ResourceProxyImpl implements ResourceProxy {

	private final Context mContext;
	
	public ResourceProxyImpl(final Context pContext) {
		mContext = pContext;
	}

	@Override
	public Drawable getDrawable(int pResId) {
		throw new IllegalArgumentException();
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
		default : throw new IllegalArgumentException();
		}
	}
}

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
		case renderer.osmarender : return mContext.getString(R.string.osmarender);
		case renderer.mapnik : return mContext.getString(R.string.mapnik);
		case renderer.cyclemap : return mContext.getString(R.string.cyclemap);
		case renderer.openareal_sat : return mContext.getString(R.string.openareal_sat);
		case renderer.base : return mContext.getString(R.string.base);
		case renderer.topo : return mContext.getString(R.string.topo);
		case renderer.hills : return mContext.getString(R.string.hills);
		case renderer.cloudmade_small : return mContext.getString(R.string.cloudmade_small);
		case renderer.cloudmade_standard : return mContext.getString(R.string.cloudmade_standard);
		default : throw new IllegalArgumentException();
		}
	}
}

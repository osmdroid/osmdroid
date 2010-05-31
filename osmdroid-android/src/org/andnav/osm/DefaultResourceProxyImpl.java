package org.andnav.osm;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class DefaultResourceProxyImpl implements ResourceProxy {

	@Override
	public String getString(int pResId) {
		switch(pResId) {
		case string.osmarender : return "OsmaRender";
		case string.mapnik : return "Mapnik";
		case string.cyclemap : return "Cycle Map";
		case string.openareal_sat : return "OpenArialMap";
		case string.base : return "OSM base layer";
		case string.topo : return "Topographic";
		case string.hills : return "Hills";
		case string.cloudmade_small : return "Cloudmade (small tiles)";
		case string.cloudmade_standard : return "Cloudmade (Standard tiles)";
		case string.unknown : return "Unknown";
		default : throw new IllegalArgumentException();
		}
	}

	@Override
	public Bitmap getBitmap(int pResId) {
		// TODO get bitmaps from the jar
		throw new IllegalArgumentException();
	}

	@Override
	public Drawable getDrawable(int pResId) {
		// TODO implementation
		throw new IllegalArgumentException();
	}

}

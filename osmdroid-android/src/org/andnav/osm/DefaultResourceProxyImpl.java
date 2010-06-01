package org.andnav.osm;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class DefaultResourceProxyImpl implements ResourceProxy {

	@Override
	public String getString(string pResId) {
		switch(pResId) {
		case osmarender : return "OsmaRender";
		case mapnik : return "Mapnik";
		case cyclemap : return "Cycle Map";
		case openareal_sat : return "OpenArialMap";
		case base : return "OSM base layer";
		case topo : return "Topographic";
		case hills : return "Hills";
		case cloudmade_small : return "Cloudmade (small tiles)";
		case cloudmade_standard : return "Cloudmade (Standard tiles)";
		case unknown : return "Unknown";
		default : throw new IllegalArgumentException();
		}
	}

	@Override
	public Bitmap getBitmap(bitmap pResId) {
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(pResId.name() + ".png");
			if (is == null) {
				throw new IllegalArgumentException();
			}
			return BitmapFactory.decodeStream(is);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException ignore) {
				}
			}
		}
	}

	@Override
	public Drawable getDrawable(drawable pResId) {
		// FIXME implementation
		// have a look at the Android source
		throw new IllegalArgumentException();
	}

}

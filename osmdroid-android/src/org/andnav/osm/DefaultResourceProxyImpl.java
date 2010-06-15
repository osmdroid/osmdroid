package org.andnav.osm;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DefaultResourceProxyImpl implements ResourceProxy {

	private DisplayMetrics mDisplayMetrics;

	/**
	 * Constructor.
	 * @param pContext Used to get the display metrics that are used for scaling the bitmaps
	 *                 returned by {@link getBitmap}.
	 *                 Can be null, in which case the bitmaps are not scaled. 
	 */
	public DefaultResourceProxyImpl(final Context pContext) {
		if (pContext != null) {
			mDisplayMetrics = new DisplayMetrics();
			final WindowManager wm = (WindowManager) pContext.getSystemService(Context.WINDOW_SERVICE);
			if (wm != null) {
				wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
			}
		}
	}
	
	@Override
	public String getString(final string pResId) {
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
	public Bitmap getBitmap(final bitmap pResId) {
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(pResId.name() + ".png");
			if (is == null) {
				throw new IllegalArgumentException();
			}
			if (mDisplayMetrics != null) {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
				options.inTargetDensity = mDisplayMetrics.densityDpi;
				return BitmapFactory.decodeStream(is, null, options);
			} else {
				return BitmapFactory.decodeStream(is);
			}
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
	public Drawable getDrawable(final bitmap pResId) {
		return new BitmapDrawable(getBitmap(pResId));
	}

}

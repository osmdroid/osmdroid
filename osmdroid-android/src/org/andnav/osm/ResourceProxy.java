package org.andnav.osm;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ResourceProxy {

	public static enum string {

		// renderers
		osmarender,
		mapnik,
		cyclemap,
		openareal_sat,
		base,
		topo,
		hills,
		cloudmade_small,
		cloudmade_standard,
		
		// other stuff
		unknown,

	}

	public static enum bitmap {

		/**
		 * For testing - the image doesn't exist.
		 */
		unknown,

		center,
		direction_arrow,
		navto_small,
		next,
		previous,
		person,
		
	}

	public static enum drawable {

		marker_default,
		marker_default_focused_base,
		
	}

	String getString(string pResId);

	Bitmap getBitmap(bitmap pResId);

	Drawable getDrawable(drawable pResId);
}

package org.osmdroid;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ResourceProxy {

	public static enum string {

	
		// other stuff
		unknown, format_distance_meters, format_distance_kilometers, format_distance_miles, format_distance_nautical_miles, format_distance_feet, online_mode, offline_mode, my_location, compass, map_mode,

	}

	public static enum bitmap {

		/**
		 * For testing - the image doesn't exist.
		 */
		unknown,

		center, direction_arrow, marker_default, marker_default_focused_base, navto_small, next, previous, person,

		/**
		 * Menu icons
		 */
		ic_menu_offline, ic_menu_mylocation, ic_menu_compass, ic_menu_mapmode
	}

	String getString(string pResId);

	/**
	 * Use a string resource as a format definition, and format using the supplied format arguments.
	 */
	String getString(string pResId, Object... formatArgs);

	Bitmap getBitmap(bitmap pResId);

	/**
	 * Get a bitmap as a {@link Drawable}
	 */
	Drawable getDrawable(bitmap pResId);

	/**
	 * Gets the density from the current screen's DisplayMetrics
	 *
	 * @return the screen's density
	 */
	float getDisplayMetricsDensity();
}

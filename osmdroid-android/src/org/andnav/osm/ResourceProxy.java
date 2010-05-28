package org.andnav.osm;

import android.graphics.drawable.Drawable;

public interface ResourceProxy {

	public static final class string {

		// renderers
		public static final int osmarender = 1;
		public static final int mapnik = 2;
		public static final int cyclemap = 3;
		public static final int openareal_sat = 4;
		public static final int base = 5;
		public static final int topo = 6;
		public static final int hills = 7;
		public static final int cloudmade_small = 8;
		public static final int cloudmade_standard = 9;
		
	}

	String getString(int pResId);

	Drawable getDrawable(int pResId);
}

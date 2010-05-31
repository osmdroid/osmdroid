package org.andnav.osm;

import android.graphics.Bitmap;
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
		
		// other stuff
		public static final int unknown = 10;

	}

	public static final class bitmap {

		public static final int zoom_in = 1;
		public static final int zoom_out = 2;
		public static final int person = 3;
		public static final int direction_arrow = 4;
		public static final int previous = 5;
		public static final int next = 6;
		public static final int center = 7;
		public static final int navto_small = 8;
		
	}

	public static final class drawable {

		public static final int marker_default = 1;
		public static final int marker_default_focused_base = 2;
		
	}

	String getString(int pResId);

	Bitmap getBitmap(int pResId);

	Drawable getDrawable(int pResId);
}

// Created by plusminus on 18:23:16 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.R;
import org.andnav.osm.services.util.OpenStreetMapTile;

/**
 * The OpenStreetMapRendererInfo stores information about available tile servers.
 * @author Nicolas Gramlich
 *
 */
public enum OpenStreetMapRendererInfo {
	OSMARENDER("http://tah.openstreetmap.org/Tiles/tile/", R.string.osmarender, ".png", 0, 17, 8, CodeScheme.X_Y),
	MAPNIK("http://tile.openstreetmap.org/", R.string.mapnik, ".png", 0, 18, 8, CodeScheme.X_Y),
	CYCLEMAP("http://b.andy.sandbox.cloudmade.com/tiles/cycle/", R.string.cyclemap, ".png", 0, 17, 8, CodeScheme.X_Y),
	OPENARIELMAP("http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/", R.string.openareal_sat, ".jpg", 0, 13, 8, CodeScheme.X_Y),
	TRAILS("http://topo.geofabrik.de/trails/", R.string.trails, ".png", 4, 17, 8, CodeScheme.X_Y),
	RELIEF("http://topo.geofabrik.de/relief/", R.string.relief, ".png", 8, 17, 8, CodeScheme.X_Y),
	CLOUDMADESMALLTILES("http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/64/", R.string.cloudmade_small, ".jpg", 0, 13, 6, CodeScheme.X_Y),
	CLOUDMADESTANDARDTILES("http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/256/", R.string.cloudmade_standard, ".jpg", 0, 18, 8, CodeScheme.X_Y);
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	public enum CodeScheme { X_Y, QUAD_TREE };
	
	public final String BASEURL, IMAGE_FILENAMEENDING;
	public final int NAME, ZOOM_MINLEVEL, ZOOM_MAXLEVEL, MAPTILE_ZOOM, MAPTILE_SIZEPX;
	public final CodeScheme CODE_SCHEME;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	private OpenStreetMapRendererInfo(final String aBaseUrl, final int aName,
			final String aImageFilenameEnding, final int aZoomMin,
			final int aZoomMax, final int aTileZoom, final CodeScheme aCodeScheme) {
		this.BASEURL = aBaseUrl;
		this.NAME = aName;
		this.ZOOM_MINLEVEL = aZoomMin;
		this.ZOOM_MAXLEVEL = aZoomMax;
		this.IMAGE_FILENAMEENDING = aImageFilenameEnding;
		this.MAPTILE_ZOOM = aTileZoom;
		this.MAPTILE_SIZEPX = 1<<aTileZoom;
		this.CODE_SCHEME = aCodeScheme;
	}
	
	public static OpenStreetMapRendererInfo getDefault() {
		return MAPNIK;
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	public String getTileURLString(final OpenStreetMapTile aTile) {
		final CodeScheme cs = this.CODE_SCHEME;
		switch (cs) {
		case QUAD_TREE:
			return String.format("%s%s%s", this.BASEURL, quadTree(aTile), this.IMAGE_FILENAMEENDING);
		case X_Y:
		default:
			return String.format("%s%d/%d/%d%s", this.BASEURL, aTile.zoomLevel, aTile.x, aTile.y, this.IMAGE_FILENAMEENDING);
		}		
	}
	
	/**
	 * Converts TMS tile coordinates to QuadTree
	 * @param aTile The tile coordinates to convert
	 * @return The QuadTree as String.
	 */
	private String quadTree(final OpenStreetMapTile aTile) {
		StringBuilder quadKey = new StringBuilder();
		for (int i = aTile.zoomLevel; i > 0; i--) {
			int digit = 0;
			int mask = 1 << (i - 1);
			if ((aTile.x & mask) != 0)
				digit += 1;
			if ((aTile.y & mask) != 0)
				digit += 2;
			quadKey.append("" + digit);
		}

		return quadKey.toString();
	}
	
}

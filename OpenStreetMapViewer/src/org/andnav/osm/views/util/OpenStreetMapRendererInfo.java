// Created by plusminus on 18:23:16 - 25.09.2008
package org.andnav.osm.views.util;

import java.util.Random;

import org.andnav.osm.R;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

/**
 * The OpenStreetMapRendererInfo stores information about available tile servers.
 * @author Nicolas Gramlich
 *
 */
public enum OpenStreetMapRendererInfo {
	OSMARENDER(R.string.osmarender, ".png", 0, 17, 8, CodeScheme.X_Y,"http://tah.openstreetmap.org/Tiles/tile/"),
	MAPNIK(R.string.mapnik, ".png", 0, 18, 8, CodeScheme.X_Y,"http://tile.openstreetmap.org/"),
	CYCLEMAP(R.string.cyclemap, ".png", 0, 17, 8, CodeScheme.X_Y,
			"http://a.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://b.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://c.andy.sandbox.cloudmade.com/tiles/cycle/"),
	OPENARIELMAP( R.string.openareal_sat, ".jpg", 0, 13, 8, CodeScheme.X_Y,"http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/"),
	BASE( R.string.base, ".png", 4, 17, 8, CodeScheme.X_Y,"http://topo.openstreetmap.de/base/"),
	TOPO(R.string.topo, ".png", 4, 17, 8, CodeScheme.X_Y,"http://topo.openstreetmap.de/topo/"),
	HILLS(R.string.hills, ".png", 8, 17, 8, CodeScheme.X_Y,"http://topo.geofabrik.de/hills/"),
	CLOUDMADESMALLTILES(R.string.cloudmade_small, ".png", 0, 13, 6, CodeScheme.X_Y,"http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/64/"),
	CLOUDMADESTANDARDTILES(R.string.cloudmade_standard, ".png", 0, 18, 8, CodeScheme.X_Y,"http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/256/");
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	public enum CodeScheme { X_Y, QUAD_TREE };
	
	public final String BASEURLS[], IMAGE_FILENAMEENDING;
	public final int NAME, ZOOM_MINLEVEL, ZOOM_MAXLEVEL, MAPTILE_ZOOM, MAPTILE_SIZEPX;
	public final CodeScheme CODE_SCHEME;
	private final Random random;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	private OpenStreetMapRendererInfo(final int aName,
			final String aImageFilenameEnding, final int aZoomMin,
			final int aZoomMax, final int aTileZoom, final CodeScheme aCodeScheme,final String ...aBaseUrl) {
		this.BASEURLS = aBaseUrl;
		this.NAME = aName;
		this.ZOOM_MINLEVEL = aZoomMin;
		this.ZOOM_MAXLEVEL = aZoomMax;
		this.IMAGE_FILENAMEENDING = aImageFilenameEnding;
		this.MAPTILE_ZOOM = aTileZoom;
		this.MAPTILE_SIZEPX = 1<<aTileZoom;
		this.CODE_SCHEME = aCodeScheme;
		this.random = new Random();
	}
	
	public static OpenStreetMapRendererInfo getDefault() {
		return MAPNIK;
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	public String getTileURLString(final OpenStreetMapTile aTile) {
		final CodeScheme cs = this.CODE_SCHEME;
		final String baseurl = BASEURLS[random.nextInt()%BASEURLS.length];
		switch (cs) {
		case QUAD_TREE:
			return String.format("%s%s%s", baseurl, quadTree(aTile), this.IMAGE_FILENAMEENDING);
		case X_Y:
		default:
			return String.format("%s%d/%d/%d%s", baseurl, aTile.getZoomLevel(), aTile.getX(), aTile.getY(), this.IMAGE_FILENAMEENDING);
		}		
	}
	
	/**
	 * Converts TMS tile coordinates to QuadTree
	 * @param aTile The tile coordinates to convert
	 * @return The QuadTree as String.
	 */
	private String quadTree(final OpenStreetMapTile aTile) {
		final StringBuilder quadKey = new StringBuilder();
		for (int i = aTile.getZoomLevel(); i > 0; i--) {
			int digit = 0;
			int mask = 1 << (i - 1);
			if ((aTile.getX() & mask) != 0)
				digit += 1;
			if ((aTile.getY() & mask) != 0)
				digit += 2;
			quadKey.append("" + digit);
		}

		return quadKey.toString();
	}
	
}

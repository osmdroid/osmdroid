// Created by plusminus on 18:23:16 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.R;
import org.andnav.osm.services.util.OpenStreetMapTile;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public enum OpenStreetMapRendererInfo {
	OSMARENDER("http://tah.openstreetmap.org/Tiles/tile/", R.string.osmarender, ".png", 0, 17, 8),
	MAPNIK("http://tile.openstreetmap.org/", R.string.mapnik, ".png", 0, 18, 8),
	CYCLEMAP("http://b.andy.sandbox.cloudmade.com/tiles/cycle/", R.string.cyclemap, ".png", 0, 17, 8),
	OPENARIELMAP("http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/", R.string.openareal_sat, ".jpg", 0, 13, 8),
	TRAILS("http://topo.geofabrik.de/trails/", R.string.trails, ".png", 4, 17, 8),
	RELIEF("http://topo.geofabrik.de/relief/", R.string.relief, ".png", 8, 17, 8),
	CLOUDMADESMALLTILES("http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/64/", R.string.cloudmade_small, ".jpg", 0, 13, 6),
	CLOUDMADESTANDARDTILES("http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/2/256/", R.string.cloudmade_standard, ".jpg", 0, 18, 8);
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	public final String BASEURL, IMAGE_FILENAMEENDING;
	public final int NAME, ZOOM_MINLEVEL, ZOOM_MAXLEVEL, MAPTILE_ZOOM, MAPTILE_SIZEPX;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	private OpenStreetMapRendererInfo(final String aBaseUrl, final int aName, final String aImageFilenameEnding, final int aZoomMin, final int aZoomMax, final int aTileZoom){
		this.BASEURL = aBaseUrl;
		this.NAME = aName;
		this.ZOOM_MINLEVEL = aZoomMin;
		this.ZOOM_MAXLEVEL = aZoomMax;
		this.IMAGE_FILENAMEENDING = aImageFilenameEnding;
		this.MAPTILE_ZOOM = aTileZoom;
		this.MAPTILE_SIZEPX = 1<<aTileZoom;
	}
	
	public static OpenStreetMapRendererInfo getDefault() {
		return MAPNIK;
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	public String getTileURLString(final OpenStreetMapTile aTile) {
		return String.format("%s%d/%d/%d%s", this.BASEURL, aTile.zoomLevel, aTile.x, aTile.y, this.IMAGE_FILENAMEENDING);
	}
}

// Created by plusminus on 18:23:16 - 25.09.2008
package org.andnav.osm.views.util;

import java.util.Random;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

/**
 * The OpenStreetMapRendererInfo stores information about available tile servers.
 * @author Nicolas Gramlich
 *
 */
public enum OpenStreetMapRendererInfo {
	OSMARENDER(ResourceProxy.string.osmarender, ".png", 0, 17, 8, CodeScheme.X_Y, "http://tah.openstreetmap.org/Tiles/tile/"),
	MAPNIK(ResourceProxy.string.mapnik, ".png", 0, 18, 8, CodeScheme.X_Y, "http://tile.openstreetmap.org/"),
	CYCLEMAP(ResourceProxy.string.cyclemap, ".png", 0, 17, 8, CodeScheme.X_Y,
			"http://a.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://b.andy.sandbox.cloudmade.com/tiles/cycle/",
			"http://c.andy.sandbox.cloudmade.com/tiles/cycle/"),
	OPENARIELMAP(ResourceProxy.string.openareal_sat, ".jpg", 0, 13, 8, CodeScheme.X_Y, "http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/"),
	BASE(ResourceProxy.string.base, ".png", 4, 17, 8, CodeScheme.X_Y, "http://topo.openstreetmap.de/base/"),
	TOPO(ResourceProxy.string.topo, ".png", 4, 17, 8, CodeScheme.X_Y, "http://topo.openstreetmap.de/topo/"),
	HILLS(ResourceProxy.string.hills, ".png", 8, 17, 8, CodeScheme.X_Y, "http://topo.geofabrik.de/hills/"),
	CLOUDMADESMALLTILES(ResourceProxy.string.cloudmade_small, ".png", 0, 13, 6, CodeScheme.CLOUDMADE, 
			"http://a.tile.cloudmade.com/%s/1/64/%d/%d/%d%s?token=%s", 
			"http://b.tile.cloudmade.com/%s/1/64/%d/%d/%d%s?token=%s", 
			"http://c.tile.cloudmade.com/%s/1/64/%d/%d/%d%s?token=%s"),
	CLOUDMADESTANDARDTILES(ResourceProxy.string.cloudmade_standard, ".png", 0, 18, 8, CodeScheme.CLOUDMADE, 
			"http://a.tile.cloudmade.com/%s/1/256/%d/%d/%d%s?token=%s", 
			"http://b.tile.cloudmade.com/%s/1/256/%d/%d/%d%s?token=%s",
			"http://c.tile.cloudmade.com/%s/1/256/%d/%d/%d%s?token=%s"),
	CLOUDMADEALTERNATIVETILES(ResourceProxy.string.cloudmade_alternative, ".png", 0, 18, 8, CodeScheme.CLOUDMADE, 
			"http://a.tile.cloudmade.com/%s/2/256/%d/%d/%d%s?token=%s", 
			"http://b.tile.cloudmade.com/%s/2/256/%d/%d/%d%s?token=%s",
			"http://c.tile.cloudmade.com/%s/2/256/%d/%d/%d%s?token=%s");
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	public enum CodeScheme { X_Y, CLOUDMADE, QUAD_TREE };
	
	public final ResourceProxy.string NAME;
	public final String BASEURLS[], IMAGE_FILENAMEENDING;
	public final int ZOOM_MINLEVEL, ZOOM_MAXLEVEL, MAPTILE_ZOOM, MAPTILE_SIZEPX;
	public final CodeScheme CODE_SCHEME;
	private final Random random;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	private OpenStreetMapRendererInfo(final ResourceProxy.string aName,
			final String aImageFilenameEnding, final int aZoomMin,
			final int aZoomMax, final int aTileZoom, final CodeScheme aCodeScheme, final String ...aBaseUrl) {
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
	
	public String getTileURLString(
			final OpenStreetMapTile aTile, 
			final IOpenStreetMapTileProviderCallback aCallback, 
			final IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback) {
		final CodeScheme cs = this.CODE_SCHEME;
		final String baseurl = BASEURLS[random.nextInt(BASEURLS.length)];
		switch (cs) {
		case QUAD_TREE:
			return baseurl + quadTree(aTile) + IMAGE_FILENAMEENDING;
		case CLOUDMADE:
			// TODO we really need some error (null) handling here
			final String key = aCallback.getCloudmadeKey();
			final String token = aCloudmadeTokenCallback.getCloudmadeToken(key);
			return String.format(baseurl, key, aTile.getZoomLevel(), aTile.getX(), aTile.getY(), IMAGE_FILENAMEENDING, token);
		case X_Y:
		default:
			return baseurl + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + IMAGE_FILENAMEENDING;
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

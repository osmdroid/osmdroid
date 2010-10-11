package org.andnav.osm.tileprovider;

import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;

/**
 * A map tile is distributed using the observer pattern.
 * The tile is delivered by a tile provider 
 * (i.e. a descendant of {@link OpenStreetMapAsyncTileProvider}  or {@link OpenStreetMapTileProvider}
 * to a consumer of tiles (e.g.  descendant of {@link OpenStreetMapTilesOverlay}).
 * Tiles are typically images (e.g. png or jpeg).
 */
public class OpenStreetMapTile {

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

	// This class must be immutable because it's used as the key in the cache hash map
	// (ie all the fields are final).
	private final IOpenStreetMapRendererInfo renderer;
	private final int x;
	private final int y;
	private final int zoomLevel;

	public OpenStreetMapTile(IOpenStreetMapRendererInfo renderer, int zoomLevel, int tileX, int tileY) {
		this.renderer = renderer;
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
	}

	public IOpenStreetMapRendererInfo getRenderer() {
		return renderer;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return renderer.name() + "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != getClass()) return false;
		final OpenStreetMapTile rhs = (OpenStreetMapTile)obj;
		return zoomLevel == rhs.zoomLevel && x == rhs.x && y == rhs.y && renderer.equals(rhs.renderer);
	}

	@Override
	public int hashCode() {
		int code = 17;
		code *= 37 + renderer.hashCode();
		code *= 37 + zoomLevel;
		code *= 37 + x;
		code *= 37 + y;
		return code;
	}

	// TODO implement equals and hashCode in renderer

}

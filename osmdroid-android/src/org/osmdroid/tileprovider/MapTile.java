package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * A map tile is distributed using the observer pattern. The tile is delivered by a tile provider
 * (i.e. a descendant of {@link MapTileModuleProviderBase} or
 * {@link MapTileProviderBase} to a consumer of tiles (e.g. descendant of
 * {@link TilesOverlay}). Tiles are typically images (e.g. png or jpeg).
 */
public class MapTile {

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

	// This class must be immutable because it's used as the key in the cache hash map
	// (ie all the fields are final).
	private final int x;
	private final int y;
	private final int zoomLevel;

	public MapTile(final int zoomLevel, final int tileX, final int tileY) {
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
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
		return "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof MapTile))
			return false;
		final MapTile rhs = (MapTile) obj;
		return zoomLevel == rhs.zoomLevel && x == rhs.x && y == rhs.y;
	}

	@Override
	public int hashCode() {
		int code = 17;
		code *= 37 + zoomLevel;
		code *= 37 + x;
		code *= 37 + y;
		return code;
	}
}

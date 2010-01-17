package org.andnav.osm.services.util;

public class OpenStreetMapTile {
	
	public int rendererID;
	
	public int x;
	public int y;
	
	public int zoomLevel;

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;
	
	public OpenStreetMapTile(int rendererID, int zoomLevel, int tileX, int tileY) {
		this.rendererID = rendererID;
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
	}
	
	@Override
	public String toString() {
		return rendererID + "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof OpenStreetMapTile) {
			OpenStreetMapTile t = (OpenStreetMapTile) o;
			return zoomLevel == t.zoomLevel && x == t.x && y == t.y && rendererID == t.rendererID;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int code = rendererID;
		code = code * 37 + zoomLevel;
		code = code * 37 + x;
		code = code * 37 + y;
		return code;
	}
	
}

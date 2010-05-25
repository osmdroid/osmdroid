package org.andnav.osm.tileprovider;

public class OpenStreetMapTile {
	
	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;
	
	private final int rendererId;
	private final int x;
	private final int y;
	private final int zoomLevel;

	public OpenStreetMapTile(int rendererId, int zoomLevel, int tileX, int tileY) {
		this.rendererId = rendererId;
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
	}
	
	public int getRendererId() {
		return rendererId;
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
		return rendererId + "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof OpenStreetMapTile) {
			OpenStreetMapTile t = (OpenStreetMapTile) o;
			return zoomLevel == t.zoomLevel && x == t.x && y == t.y && rendererId == t.rendererId;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int code = 17;
		code *= 37 + rendererId;
		code *= 37 + zoomLevel;
		code *= 37 + x;
		code *= 37 + y;
		return code;
	}
	
}

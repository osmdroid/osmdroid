package org.andnav.osm.tileprovider;


public class OpenStreetMapTile {

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

	private final String rendererName;
	private final int x;
	private final int y;
	private final int zoomLevel;

	public OpenStreetMapTile(String rendererName, int zoomLevel, int tileX, int tileY) {
		this.rendererName = rendererName;
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
	}

	public String getRendererName() {
		return rendererName;
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
		return rendererName + "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != getClass()) return false;
		final OpenStreetMapTile rhs = (OpenStreetMapTile)obj;
		return zoomLevel == rhs.zoomLevel && x == rhs.x && y == rhs.y && rendererName.equals(rhs.rendererName);
	}

	@Override
	public int hashCode() {
		int code = 17;
		code *= 37 + rendererName.hashCode();
		code *= 37 + zoomLevel;
		code *= 37 + x;
		code *= 37 + y;
		return code;
	}

}

package org.andnav.osm.services.util;

public class OpenStreetMapTile {
	
	public int rendererID;
	
	public int x;
	public int y;
	
	public int zoomLevel;
	
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

}

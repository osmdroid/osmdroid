package org.osmdroid.debug.model;

import org.osmdroid.tileprovider.MapTile;

/**
 * extending map tile to provide source and database key. note: x,y,z coordinates are not used
 * created on 12/20/2016.
 *
 * @author Alex O'Ree
 * @since 5.6.2
 */

public class MapTileExt extends MapTile {
    public MapTileExt(int zoomLevel, int tileX, int tileY) {
        super(zoomLevel, tileX, tileY);
    }

    public String source;
    public long key = 0;

}

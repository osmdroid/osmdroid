package org.osmdroid.debug.model;

/**
 * Provide source, database key and expiration date to map tile.
 * created on 12/20/2016.
 * There use to be a `MapTile`. Not anymore, we use {@link org.osmdroid.util.MapTileIndex} instead.
 *
 * @author Alex O'Ree
 * @since 5.6.2
 */

public class MapTileExt {
    public String source;
    public long key;
    public Long expires;
}

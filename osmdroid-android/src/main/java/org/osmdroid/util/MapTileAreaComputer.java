package org.osmdroid.util;

/**
 * Compute a map tile area from a map tile area source
 * @since 6.0.3
 * @author Fabrice Fontaine
 */

public interface MapTileAreaComputer {

    MapTileArea computeFromSource(final MapTileArea pSource, final MapTileArea pReuse);
}

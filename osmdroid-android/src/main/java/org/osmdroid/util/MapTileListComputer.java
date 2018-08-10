package org.osmdroid.util;

/**
 * Compute a map tile list from a map tile list source
 * @since 6.0.2
 * @author Fabrice Fontaine
 * @deprecated Use {@link MapTileAreaComputer} instead
 */

@Deprecated
public interface MapTileListComputer {

    MapTileList computeFromSource(final MapTileList pSource, final MapTileList pReuse);
}

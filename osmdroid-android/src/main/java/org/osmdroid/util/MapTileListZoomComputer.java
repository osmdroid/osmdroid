package org.osmdroid.util;

/**
 * Compute a map tile list from a map tile list source, on another zoom level
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 * @deprecated Use {@link MapTileAreaZoomComputer} instead
 */

@Deprecated
public class MapTileListZoomComputer implements MapTileListComputer {

    private final int mZoomDelta;

    public MapTileListZoomComputer(final int pZoomDelta) {
        mZoomDelta = pZoomDelta;
    }

    public int getZoomDelta() {
        return mZoomDelta;
    }

    @Override
    public MapTileList computeFromSource(final MapTileList pSource, final MapTileList pReuse) {
        final MapTileList out = pReuse != null ? pReuse : new MapTileList();
        for (int i = 0; i < pSource.getSize(); i++) {
            final long sourceIndex = pSource.get(i);
            final int sourceZoom = MapTileIndex.getZoom(sourceIndex);
            final int destZoom = sourceZoom + mZoomDelta;
            if (destZoom < 0 || destZoom > MapTileIndex.mMaxZoomLevel) {
                continue;
            }
            final int sourceX = MapTileIndex.getX(sourceIndex);
            final int sourceY = MapTileIndex.getY(sourceIndex);
            if (mZoomDelta <= 0) {
                out.put(MapTileIndex.getTileIndex(destZoom, sourceX >> -mZoomDelta, sourceY >> -mZoomDelta));
                continue;
            }
            final int power = 1 << mZoomDelta;
            final int destX = sourceX << mZoomDelta;
            final int destY = sourceY << mZoomDelta;
            for (int j = 0; j < power; j++) {
                for (int k = 0; k < power; k++) {
                    out.put(MapTileIndex.getTileIndex(destZoom, destX + j, destY + k));
                }
            }
        }
        return out;
    }
}

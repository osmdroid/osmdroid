package org.osmdroid.util;

/**
 * Compute a map tile area from a map tile area source: the source on another zoom level
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaZoomComputer implements MapTileAreaComputer {

    private final int mZoomDelta;

    public MapTileAreaZoomComputer(final int pZoomDelta) {
        mZoomDelta = pZoomDelta;
    }

    @Override
    public MapTileArea computeFromSource(final MapTileArea pSource, final MapTileArea pReuse) {
        final MapTileArea out = pReuse != null ? pReuse : new MapTileArea();
        if (pSource.size() == 0) {
            out.reset();
            return out;
        }
        final int sourceZoom = pSource.getZoom();
        int destZoom = sourceZoom + mZoomDelta;
        if (destZoom < 0 || destZoom > MapTileIndex.mMaxZoomLevel) {
            out.reset();
            return out;
        }
        if (mZoomDelta <= 0) {
            out.set(destZoom,
                    pSource.getLeft() >> -mZoomDelta, pSource.getTop() >> -mZoomDelta,
                    pSource.getRight() >> -mZoomDelta, pSource.getBottom() >> -mZoomDelta);
            return out;
        }
        out.set(destZoom,
                pSource.getLeft() << mZoomDelta, pSource.getTop() << mZoomDelta,
                ((1 + pSource.getRight()) << mZoomDelta) - 1, ((1 + pSource.getBottom()) << mZoomDelta) - 1
        );
        return out;
    }
}

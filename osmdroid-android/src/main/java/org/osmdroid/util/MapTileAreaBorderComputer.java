package org.osmdroid.util;

/**
 * Compute a map tile area from a map tile area source: the source with a border
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public class MapTileAreaBorderComputer implements MapTileAreaComputer {

    private final int mBorder;

    public MapTileAreaBorderComputer(final int pBorder) {
        mBorder = pBorder;
    }

    public int getBorder() {
        return mBorder;
    }

    @Override
    public MapTileArea computeFromSource(final MapTileArea pSource, final MapTileArea pReuse) {
        final MapTileArea out = pReuse != null ? pReuse : new MapTileArea();
        if (pSource.size() == 0) {
            out.reset();
            return out;
        }
        final int left = pSource.getLeft() - mBorder;
        final int top = pSource.getTop() - mBorder;
        final int additional = 2 * mBorder - 1;
        out.set(pSource.getZoom(),
                left, top,
                left + pSource.getWidth() + additional, top + pSource.getHeight() + additional);
        return out;
    }
}

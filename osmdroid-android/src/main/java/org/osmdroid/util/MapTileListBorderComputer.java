package org.osmdroid.util;

/**
 * Compute a map tile list from a map tile list source: its border
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 * @deprecated Use {@link MapTileAreaBorderComputer} instead
 */

@Deprecated
public class MapTileListBorderComputer implements MapTileListComputer {

    private final int mBorder;
    private final boolean mIncludeAll;

    public MapTileListBorderComputer(final int pBorder, final boolean pIncludeAll) {
        mBorder = pBorder;
        mIncludeAll = pIncludeAll;
    }

    public int getBorder() {
        return mBorder;
    }

    public boolean isIncludeAll() {
        return mIncludeAll;
    }

    @Override
    public MapTileList computeFromSource(final MapTileList pSource, final MapTileList pReuse) {
        final MapTileList out = pReuse != null ? pReuse : new MapTileList();
        for (int i = 0; i < pSource.getSize(); i++) {
            final long sourceIndex = pSource.get(i);
            final int zoom = MapTileIndex.getZoom(sourceIndex);
            final int sourceX = MapTileIndex.getX(sourceIndex);
            final int sourceY = MapTileIndex.getY(sourceIndex);
            final int power = 1 << zoom;
            for (int j = -mBorder; j <= mBorder; j++) {
                for (int k = -mBorder; k <= mBorder; k++) {
                    int destX = sourceX + j;
                    int destY = sourceY + k;
                    while (destX < 0) {
                        destX += power;
                    }
                    while (destY < 0) {
                        destY += power;
                    }
                    while (destX >= power) {
                        destX -= power;
                    }
                    while (destY >= power) {
                        destY -= power;
                    }
                    final long index = MapTileIndex.getTileIndex(zoom, destX, destY);
                    if (out.contains(index)) {
                        continue;
                    }
                    if (pSource.contains(index) && !mIncludeAll) {
                        continue;
                    }
                    out.put(index);
                }
            }
        }
        return out;
    }
}

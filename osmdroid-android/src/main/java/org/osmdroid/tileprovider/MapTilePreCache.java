package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.modules.CantContinueException;
import org.osmdroid.tileprovider.modules.MapTileDownloaderProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.OutOfAllowedZoomException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GarbageCollector;
import org.osmdroid.util.MapTileArea;
import org.osmdroid.util.MapTileAreaList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Tile Pre Cache goal is to:
 * - list the tiles that are near the ones that are currently displayed (border, zoom+-1, ...)
 * - try to find their bitmap using a list of providers
 * - pre-cache those bitmaps into memory cache
 * Doing so you get smoother when panning the map or zooming in/out
 * as the bitmaps are already in memory.
 * Cf. <a href="https://github.com/osmdroid/osmdroid/issues/930">#930</a>
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */

public class MapTilePreCache {

    private final List<MapTileModuleProviderBase> mProviders = new ArrayList<>(); // cf. MapTileApproximater
    private final MapTileAreaList mTileAreas = new MapTileAreaList();
    private Iterator<Long> mTileIndices;
    private final MapTileCache mCache;
    private final GarbageCollector mGC = new GarbageCollector(() -> {
        long next;
        while ((next = next()) != -1) {
            search(next);
        }
    });

    public MapTilePreCache(final MapTileCache pCache) {
        mCache = pCache;
    }

    public void addProvider(final MapTileModuleProviderBase pProvider) {
        mProviders.add(pProvider);
    }

    /**
     * Compute the latest tile list and try to put each tile bitmap in memory cache
     */
    public void fill() {
        if (mGC.isRunning()) {
            return;
        }
        refresh();
        mGC.gc();
    }

    /**
     * Refresh the tile list in the synchronized way
     * so that the asynchronous running GC actually get the actual next tile
     * when calling method {@link #next()}
     */
    private void refresh() {
        synchronized (mTileAreas) {
            int index = 0;
            final List<MapTileArea> cMapTitleAreas = mTileAreas.getList();
            for (final MapTileArea area : mCache.getAdditionalMapTileList().getList()) {
                final MapTileArea copy;
                if (index < cMapTitleAreas.size()) {
                    copy = cMapTitleAreas.get(index);
                } else {
                    copy = new MapTileArea();
                    cMapTitleAreas.add(copy);
                }
                copy.set(area);
                index++;
            }
            while (index < cMapTitleAreas.size()) {
                cMapTitleAreas.remove(cMapTitleAreas.size() - 1);
            }
            mTileIndices = mTileAreas.iterator();
        }
    }

    /**
     * Get the next tile to search for
     *
     * @return -1 if not found
     */
    private long next() {
        while (true) {
            final long index;
            synchronized (mTileAreas) {
                if (!mTileIndices.hasNext()) {
                    return -1;
                }
                index = mTileIndices.next();
            }
            final Drawable drawable = mCache.getMapTile(index);
            if (drawable == null) {
                return index;
            }
        }
    }

    /**
     * Search for a tile bitmap into the list of providers and put it in the memory cache
     */
    private void search(final long pMapTileIndex) {
        for (final MapTileModuleProviderBase provider : mProviders) {
            try {
                if (provider instanceof MapTileDownloaderProvider) {
                    final ITileSource tileSource = ((MapTileDownloaderProvider) provider).getTileSource();
                    if (tileSource instanceof OnlineTileSourceBase) {
                        if (!((OnlineTileSourceBase) tileSource).getTileSourcePolicy().acceptsPreventive()) {
                            continue;
                        }
                    }
                }
                final Drawable drawable = provider.getTileLoader().loadTileIfReachable(pMapTileIndex);
                if (drawable == null) continue;
                mCache.putTile(pMapTileIndex, drawable);
                return;
            } catch (CantContinueException | OutOfAllowedZoomException exception) {
                // just dismiss that lazily: we don't need to be severe here
            }
        }
    }
}

package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileContainer;
import org.osmdroid.util.MapTileIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This top-level tile provider allows a consumer to provide an array of modular asynchronous tile
 * providers to be used to obtain map tiles. When a tile is requested, the
 * {@link MapTileProviderArray} first checks the {@link MapTileCache} (synchronously) and returns
 * the tile if available. If not, then the {@link MapTileProviderArray} returns null and sends the
 * tile request through the asynchronous tile request chain. Each asynchronous tile provider returns
 * success/failure to the {@link MapTileProviderArray}. If successful, the
 * {@link MapTileProviderArray} passes the result to the base class. If failed, then the next
 * asynchronous tile provider is called in the chain. If there are no more asynchronous tile
 * providers in the chain, then the failure result is passed to the base class. The
 * {@link MapTileProviderArray} provides a mechanism so that only one unique tile-request can be in
 * the map tile request chain at a time.
 *
 * @author Marc Kurtz
 */
public class MapTileProviderArray extends MapTileProviderBase implements MapTileContainer {

    private final Map<Long, Integer> mWorking = new HashMap<>();
    private IRegisterReceiver mRegisterReceiver = null;
    protected final List<MapTileModuleProviderBase> mTileProviderList;

    /**
     * @since 6.0.2
     */
    private static final int WORKING_STATUS_STARTED = 0;
    private static final int WORKING_STATUS_FOUND = 1;

    /**
     * Creates an {@link MapTileProviderArray} with no tile providers.
     *
     * @param pRegisterReceiver a {@link IRegisterReceiver}
     */
    protected MapTileProviderArray(final ITileSource pTileSource,
                                   final IRegisterReceiver pRegisterReceiver) {
        this(pTileSource, pRegisterReceiver, new MapTileModuleProviderBase[0]);
    }

    /**
     * Creates an {@link MapTileProviderArray} with the specified tile providers.
     *
     * @param aRegisterReceiver  a {@link IRegisterReceiver}
     * @param pTileProviderArray an array of {@link MapTileModuleProviderBase}
     */
    public MapTileProviderArray(final ITileSource pTileSource,
                                final IRegisterReceiver aRegisterReceiver,
                                final MapTileModuleProviderBase[] pTileProviderArray) {
        super(pTileSource);

        mRegisterReceiver = aRegisterReceiver;
        mTileProviderList = new ArrayList<>();
        Collections.addAll(mTileProviderList, pTileProviderArray);
    }

    @Override
    public void detach() {
        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                tileProvider.detach();

            }
        }
        synchronized (mWorking) {
            mWorking.clear();
        }
        if (mRegisterReceiver != null) {
            mRegisterReceiver.destroy();
            mRegisterReceiver = null;
        }
        super.detach();
    }

    /**
     * @since 6.0.2
     */
    @Override
    public boolean contains(long pTileIndex) {
        synchronized (mWorking) {
            return mWorking.containsKey(pTileIndex);
        }
    }

    /**
     * @since 6.0
     * @deprecated Not used anymore. Use {@link #isDowngradedMode(long)} instead
     */
    @Deprecated
    protected boolean isDowngradedMode() {
        return false;
    }

    /**
     * @since 6.0.3
     */
    protected boolean isDowngradedMode(final long pMapTileIndex) {
        return false;
    }

    @Override
    public Drawable getMapTile(final long pMapTileIndex) {
        final Drawable tile = mTileCache.getMapTile(pMapTileIndex);
        if (tile != null) {
            if (ExpirableBitmapDrawable.getState(tile) == ExpirableBitmapDrawable.UP_TO_DATE) {
                return tile; // best scenario ever
            }
            if (isDowngradedMode(pMapTileIndex)) {
                return tile; // best we can, considering
            }
        }

        synchronized (mWorking) {
            if (mWorking.containsKey(pMapTileIndex)) {
                return tile;
            }
            mWorking.put(pMapTileIndex, WORKING_STATUS_STARTED);
        }

        final MapTileRequestState state = new MapTileRequestState(pMapTileIndex, mTileProviderList, MapTileProviderArray.this);
        runAsyncNextProvider(state);

        return tile;
    }

    /**
     * @since 6.0.0
     */
    private void remove(final long pMapTileIndex) {
        synchronized (mWorking) {
            mWorking.remove(pMapTileIndex);
        }
    }

    @Override
    public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
        super.mapTileRequestCompleted(aState, aDrawable);
        remove(aState.getMapTile());
    }

    @Override
    public void mapTileRequestFailed(final MapTileRequestState aState) {
        runAsyncNextProvider(aState);
    }

    @Override
    public void mapTileRequestFailedExceedsMaxQueueSize(final MapTileRequestState aState) {
        super.mapTileRequestFailed(aState);
        remove(aState.getMapTile());
    }

    @Override
    public void mapTileRequestExpiredTile(MapTileRequestState aState, Drawable aDrawable) {
        super.mapTileRequestExpiredTile(aState, aDrawable);
        synchronized (mWorking) {
            mWorking.put(aState.getMapTile(), WORKING_STATUS_FOUND);
        }

        // Continue through the provider chain
        runAsyncNextProvider(aState);
    }

    @Override
    public IFilesystemCache getTileWriter() {
        return null;
    }

    @Override
    public long getQueueSize() {
        synchronized (mWorking) {
            return mWorking.size();
        }
    }

    /**
     * We want to not use a provider that doesn't exist anymore in the chain, and we want to not use
     * a provider that requires a data connection when one is not available.
     */
    protected MapTileModuleProviderBase findNextAppropriateProvider(final MapTileRequestState aState) {
        MapTileModuleProviderBase provider;
        boolean providerDoesntExist = false, providerCantGetDataConnection = false, providerCantServiceZoomlevel = false;
        // The logic of the while statement is
        // "Keep looping until you get null, or a provider that still exists
        // and has a data connection if it needs one and can service the zoom level,"
        do {
            provider = aState.getNextProvider();
            // Perform some checks to see if we can use this provider
            // If any of these are true, then that disqualifies the provider for this tile request.
            if (provider != null) {
                providerDoesntExist = !this.getProviderExists(provider);
                providerCantGetDataConnection = !useDataConnection()
                        && provider.getUsesDataConnection();
                int zoomLevel = MapTileIndex.getZoom(aState.getMapTile());
                providerCantServiceZoomlevel = zoomLevel > provider.getMaximumZoomLevel()
                        || zoomLevel < provider.getMinimumZoomLevel();
            }
        } while ((provider != null)
                && (providerDoesntExist || providerCantGetDataConnection || providerCantServiceZoomlevel));
        return provider;
    }

    /**
     * @since 6.0.2
     */
    private void runAsyncNextProvider(final MapTileRequestState pState) {
        final MapTileModuleProviderBase nextProvider = findNextAppropriateProvider(pState);
        if (nextProvider != null) {
            nextProvider.loadMapTileAsync(pState);
            return;
        }
        final Integer status; // as Integer (and not int) for concurrency reasons
        synchronized (mWorking) {
            status = mWorking.get(pState.getMapTile());
        }
        if (status != null && status == WORKING_STATUS_STARTED) {
            super.mapTileRequestFailed(pState);
        }
        remove(pState.getMapTile());
    }

    public boolean getProviderExists(final MapTileModuleProviderBase provider) {
        return mTileProviderList.contains(provider);
    }

    @Override
    public int getMinimumZoomLevel() {
        int result = org.osmdroid.util.TileSystem.getMaximumZoomLevel();
        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                if (tileProvider.getMinimumZoomLevel() < result) {
                    result = tileProvider.getMinimumZoomLevel();
                }
            }
        }
        return result;
    }

    @Override
    public int getMaximumZoomLevel() {
        int result = OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL;
        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                if (tileProvider.getMaximumZoomLevel() > result) {
                    result = tileProvider.getMaximumZoomLevel();
                }
            }
        }
        return result;
    }

    @Override
    public void setTileSource(final ITileSource aTileSource) {
        super.setTileSource(aTileSource);

        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                tileProvider.setTileSource(aTileSource);
                clearTileCache();
            }
        }
    }
}

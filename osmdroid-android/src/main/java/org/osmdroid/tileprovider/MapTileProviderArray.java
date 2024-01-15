package org.osmdroid.tileprovider;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.ReusablePoolDynamic;
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
    private final ReusablePoolDynamic<Long,MapTileRequestState> mReusablePoolDynamic;

    /**
     * @since 6.0.2
     */
    private static final int WORKING_STATUS_STARTED = 0;
    private static final int WORKING_STATUS_FOUND = 1;

    /** Creates an {@link MapTileProviderArray} with no tile providers */
    protected MapTileProviderArray(@NonNull final Context context,
                                   final ITileSource pTileSource,
                                   final IRegisterReceiver pRegisterReceiver) {
        this(context, pTileSource, pRegisterReceiver, new MapTileModuleProviderBase[0]);
    }
    /**
     * Creates an {@link MapTileProviderArray} with the specified tile providers.
     *
     * @param aRegisterReceiver  a {@link IRegisterReceiver}
     * @param pTileProviderArray an array of {@link MapTileModuleProviderBase}
     */
    public MapTileProviderArray(@NonNull final Context context,
                                final ITileSource pTileSource,
                                final IRegisterReceiver aRegisterReceiver,
                                final MapTileModuleProviderBase[] pTileProviderArray) {
        super(context, pTileSource);

        mRegisterReceiver = aRegisterReceiver;
        mTileProviderList = new ArrayList<>();
        Collections.addAll(mTileProviderList, pTileProviderArray);
        mReusablePoolDynamic = new ReusablePoolDynamic<>(new ReusablePoolDynamic.ReusableIndexCallback<>() {
            @Override
            public ReusablePoolDynamic.ReusableItemSetInterface<Long> newInstance() {
                return new MapTileRequestState(null);
            }
        }, 32);
    }

    @Override
    public void onDetach(@NonNull final Context context) {
        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                tileProvider.detach(context);
            }
        }
        synchronized (mWorking) {
            mWorking.clear();
        }
        if (mRegisterReceiver != null) {
            mRegisterReceiver.destroy();
            mRegisterReceiver = null;
        }
        super.onDetach(context);
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

        final MapTileRequestState cCachedItem = mReusablePoolDynamic.getFreeItemAndSet(pMapTileIndex);
        runAsyncNextProvider(cCachedItem, MapTileProviderArray.this);

        return tile;
    }

    /**
     * @since 6.0.0
     */
    private void remove(final MapTileRequestState pState) {
        final Long cMapTileIndex = pState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        synchronized (mWorking) {
            mWorking.remove(cMapTileIndex);
        }
        mReusablePoolDynamic.setItemElegibleToBeFreed(pState, false);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestStarted(final MapTileRequestState aState, final int pending, final int working) {
        super.mapTileRequestStarted(aState, pending, working);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
        super.mapTileRequestCompleted(aState, aDrawable);
        remove(aState);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestFailed(final MapTileRequestState aState) {
        super.mapTileRequestFailed(aState);
        runAsyncNextProvider(aState, MapTileProviderArray.this);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestFailedExceedsMaxQueueSize(final MapTileRequestState aState) {
        super.mapTileRequestFailedExceedsMaxQueueSize(aState);
        remove(aState);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestExpiredTile(MapTileRequestState aState, Drawable aDrawable) {
        super.mapTileRequestExpiredTile(aState, aDrawable);
        final Long cMapTileIndex = aState.getMapTileIndex();
        if (cMapTileIndex == null) return;
        synchronized (mWorking) {
            mWorking.put(cMapTileIndex, WORKING_STATUS_FOUND);
        }

        // Continue through the provider chain
        runAsyncNextProvider(aState, MapTileProviderArray.this);
    }

    /** {@inheritDoc} */
    @Override
    public void mapTileRequestDiscartedDueToOutOfViewBounds(MapTileRequestState aState) {
        super.mapTileRequestDiscartedDueToOutOfViewBounds(aState);
        remove(aState);
    }

    /** {@inheritDoc} */
    @Override
    public IFilesystemCache getTileWriter() {
        return null;
    }

    /** {@inheritDoc} */
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
    @Nullable
    protected MapTileModuleProviderBase findNextAppropriateProvider(@NonNull final MapTileRequestState aState) {
        MapTileModuleProviderBase provider;
        boolean providerDoesntExist = false, providerCantGetDataConnection = false, providerCantServiceZoomlevel = false;
        // The logic of the while statement is
        // "Keep looping until you get null, or a provider that still exists
        // and has a data connection if it needs one and can service the zoom level,"
        int zoomLevel;
        do {
            provider = aState.getNextProvider(mTileProviderList);
            // Perform some checks to see if we can use this provider
            // If any of these are true, then that disqualifies the provider for this tile request.
            if (provider != null) {
                providerDoesntExist = !this.getProviderExists(provider);
                providerCantGetDataConnection = !useDataConnection() && provider.getUsesDataConnection();
                //noinspection DataFlowIssue
                zoomLevel = MapTileIndex.getZoom(aState.getMapTileIndex());
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
    private void runAsyncNextProvider(@Nullable final MapTileRequestState pState, @NonNull final IMapTileProviderCallback callback) {
        final Long cMapTileIndex;
        if ((pState == null) || ((cMapTileIndex = pState.getMapTileIndex()) == null)) return;
        final MapTileModuleProviderBase nextProvider = findNextAppropriateProvider(pState);
        if (nextProvider != null) {
            nextProvider.loadMapTileAsync(pState, callback);
            return;
        }
        final Integer status;
        synchronized (mWorking) {
            status = mWorking.get(cMapTileIndex);
        }
        if (status != null) {
            switch (status) {
                case WORKING_STATUS_STARTED: {
                    super.mapTileRequestFailed(pState);
                    break;
                }
                case WORKING_STATUS_FOUND: {
                    super.mapTileRequestDoneButUnknown(pState); //<-- needed to notify a "finish/done" operation on a State because
                                                                // for each "Loading X,Y,Z tile" action a corresponding "Completed",
                                                                // "Failed" OR a simple "Done" action is needed for "close" that specific request
                    break;
                }
            }
        }
        remove(pState);
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
    public void setTileSource(@NonNull final ITileSource aTileSource) {
        super.setTileSource(aTileSource);

        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                tileProvider.setTileSource(aTileSource);
                clearTileCache();
            }
        }
    }

    @Override
    public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) {
        synchronized (mTileProviderList) {
            for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
                tileProvider.onViewBoundingBoxChanged(fromBounds, fromZoom, toBounds, toZoom);
            }
        }
    }
}

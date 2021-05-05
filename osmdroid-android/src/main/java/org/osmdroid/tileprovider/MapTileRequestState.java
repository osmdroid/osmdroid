package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapTileRequestState {

    private final List<MapTileModuleProviderBase> mProviderQueue;
    private final long mMapTileIndex;
    private final IMapTileProviderCallback mCallback;
    private int index;
    private MapTileModuleProviderBase mCurrentProvider;

    /**
     * @deprecated use {@link MapTileRequestState#MapTileRequestState(long, List, IMapTileProviderCallback)}  instead
     */
    @Deprecated
    public MapTileRequestState(final long pMapTleIndex,
                               final MapTileModuleProviderBase[] providers,
                               final IMapTileProviderCallback callback) {
        mProviderQueue = new ArrayList<>();
        Collections.addAll(mProviderQueue, providers);
        mMapTileIndex = pMapTleIndex;
        mCallback = callback;
    }

    /**
     * @since 6.0
     */
    public MapTileRequestState(final long pMapTileIndex,
                               final List<MapTileModuleProviderBase> providers,
                               final IMapTileProviderCallback callback) {
        mProviderQueue = providers;
        mMapTileIndex = pMapTileIndex;
        mCallback = callback;
    }

    /**
     * @since 6.0.0
     */
    public long getMapTile() {
        return mMapTileIndex;
    }

    public IMapTileProviderCallback getCallback() {
        return mCallback;
    }

    public boolean isEmpty() {
        return mProviderQueue == null || index >= mProviderQueue.size();
    }

    public MapTileModuleProviderBase getNextProvider() {
        mCurrentProvider = isEmpty() ? null : mProviderQueue.get(index++);
        return mCurrentProvider;
    }

    public MapTileModuleProviderBase getCurrentProvider() {
        return mCurrentProvider;
    }
}

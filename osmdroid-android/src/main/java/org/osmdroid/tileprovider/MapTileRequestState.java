package org.osmdroid.tileprovider;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.util.ReusablePoolDynamic;

import java.util.List;

public final class MapTileRequestState implements ReusablePoolDynamic.ReusableItemSetInterface<Long> {

    @Nullable
    private Long mMapTileIndex = null;
    private int mCurrentProviderListIndex = 0;
    @Nullable
    private MapTileModuleProviderBase mCurrentProvider;
    private Long mStartLoadingMillis = 0L;

    /**
     * @deprecated use {@link MapTileRequestState#MapTileRequestState(Long)} instead
     */
    @Deprecated
    public MapTileRequestState(final long pMapTileIndex,
                               final MapTileModuleProviderBase[] providers,
                               final IMapTileProviderCallback callback) {
        mMapTileIndex = pMapTileIndex;
    }

    /**
     * @since 6.0
     * @deprecated use {@link MapTileRequestState#MapTileRequestState(Long)} instead
     */
    @Deprecated
    public MapTileRequestState(@Nullable final Long pMapTileIndex,
                               final List<MapTileModuleProviderBase> providers,
                               final IMapTileProviderCallback callback) {
        mMapTileIndex = pMapTileIndex;
    }
    /**
     * @since 6.1.18
     */
    public MapTileRequestState(@Nullable final Long pMapTileIndex) {
        mMapTileIndex = pMapTileIndex;
    }

    /**
     * @since 6.0.0
     */
    @Nullable
    public Long getMapTileIndex() {
        return mMapTileIndex;
    }

    @Nullable
    public MapTileModuleProviderBase getNextProvider(@Nullable final List<MapTileModuleProviderBase> providersList) {
        mCurrentProvider = ((providersList == null) || (mCurrentProviderListIndex >= providersList.size())) ? null : providersList.get(mCurrentProviderListIndex++);
        return mCurrentProvider;
    }

    @Nullable
    public MapTileModuleProviderBase getCurrentProvider() {
        return mCurrentProvider;
    }

    public long getLoadingTimeMillis() {
        return (SystemClock.elapsedRealtime() - mStartLoadingMillis);
    }

    @Nullable
    @Override
    public Long getKey() { return mMapTileIndex; }

    @Override
    public void set(@NonNull final Long key) {
        mMapTileIndex = key;
        mStartLoadingMillis = SystemClock.elapsedRealtime();
        //------ same as reset() ------
        mCurrentProviderListIndex = 0;
        mCurrentProvider = null;
        //-----------------------------
    }

    @Override
    public void reset() {
        mMapTileIndex = null;
        mStartLoadingMillis = 0L;
        //------ same as set() ------
        mCurrentProviderListIndex = 0;
        mCurrentProvider = null;
        //---------------------------
    }

    @Override
    public void freeMemory() { /*nothing*/ }
}

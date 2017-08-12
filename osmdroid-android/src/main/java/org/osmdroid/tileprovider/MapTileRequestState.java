package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapTileRequestState {

	private final List<MapTileModuleProviderBase> mProviderQueue;
	private final MapTile mMapTile;
	private final IMapTileProviderCallback mCallback;
	private int index;
	private MapTileModuleProviderBase mCurrentProvider;

	/**
	 * @deprecated use {@link MapTileRequestState#MapTileRequestState(MapTile, List, IMapTileProviderCallback)}  instead
	 */
	@Deprecated
	public MapTileRequestState(final MapTile mapTile,
							   final MapTileModuleProviderBase[] providers,
							   final IMapTileProviderCallback callback) {
		mProviderQueue = new ArrayList<>();
		Collections.addAll(mProviderQueue, providers);
		mMapTile = mapTile;
		mCallback = callback;
	}

	/**
	 * @since 6.0
	 */
	public MapTileRequestState(final MapTile mapTile,
							   final List<MapTileModuleProviderBase> providers,
							   final IMapTileProviderCallback callback) {
		mProviderQueue = providers;
		mMapTile = mapTile;
		mCallback = callback;
	}

	public MapTile getMapTile() {
		return mMapTile;
	}

	public IMapTileProviderCallback getCallback() {
		return mCallback;
	}

	public boolean isEmpty() {
		return mProviderQueue == null || index >= mProviderQueue.size();
	}

	public MapTileModuleProviderBase getNextProvider() {
		mCurrentProvider = isEmpty() ? null : mProviderQueue.get(index ++);
		return mCurrentProvider;
	}

	public MapTileModuleProviderBase getCurrentProvider() {
		return mCurrentProvider;
	}
}

package org.osmdroid.tileprovider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.osmdroid.tileprovider.modules.OpenStreetMapTileModuleProviderBase;

public class MapTileRequestState {

	private final Queue<OpenStreetMapTileModuleProviderBase> mProviderQueue;
	private final MapTile mMapTile;
	private final IMapTileProviderCallback mCallback;
	private OpenStreetMapTileModuleProviderBase mCurrentProvider;

	public MapTileRequestState(final MapTile mapTile,
			final OpenStreetMapTileModuleProviderBase[] providers,
			final IMapTileProviderCallback callback) {
		mProviderQueue = new LinkedList<OpenStreetMapTileModuleProviderBase>();
		Collections.addAll(mProviderQueue, providers);
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
		return mProviderQueue.isEmpty();
	}

	public OpenStreetMapTileModuleProviderBase getNextProvider() {
		mCurrentProvider = mProviderQueue.poll();
		return mCurrentProvider;
	}

	public OpenStreetMapTileModuleProviderBase getCurrentProvider() {
		return mCurrentProvider;
	}
}

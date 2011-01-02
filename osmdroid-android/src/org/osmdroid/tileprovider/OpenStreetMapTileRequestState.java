package org.osmdroid.tileprovider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.osmdroid.tileprovider.modules.OpenStreetMapTileModuleProviderBase;

public class OpenStreetMapTileRequestState {

	private final Queue<OpenStreetMapTileModuleProviderBase> mProviderQueue;
	private final OpenStreetMapTile mMapTile;
	private final IOpenStreetMapTileProviderCallback mCallback;
	private OpenStreetMapTileModuleProviderBase mCurrentProvider;

	public OpenStreetMapTileRequestState(final OpenStreetMapTile mapTile,
			final OpenStreetMapTileModuleProviderBase[] providers,
			final IOpenStreetMapTileProviderCallback callback) {
		mProviderQueue = new LinkedList<OpenStreetMapTileModuleProviderBase>();
		Collections.addAll(mProviderQueue, providers);
		mMapTile = mapTile;
		mCallback = callback;
	}

	public OpenStreetMapTile getMapTile() {
		return mMapTile;
	}

	public IOpenStreetMapTileProviderCallback getCallback() {
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

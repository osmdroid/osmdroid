package org.osmdroid.tileprovider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.osmdroid.tileprovider.modules.OpenStreetMapAsyncTileProvider;

public class OpenStreetMapTileRequestState {

	private final Queue<OpenStreetMapAsyncTileProvider> mProviderQueue;
	private final OpenStreetMapTile mMapTile;
	private final IOpenStreetMapTileProviderCallback mCallback;
	private OpenStreetMapAsyncTileProvider mCurrentProvider;

	public OpenStreetMapTileRequestState(final OpenStreetMapTile mapTile,
			final OpenStreetMapAsyncTileProvider[] providers,
			final IOpenStreetMapTileProviderCallback callback) {
		mProviderQueue = new LinkedList<OpenStreetMapAsyncTileProvider>();
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

	public OpenStreetMapAsyncTileProvider getNextProvider() {
		mCurrentProvider = mProviderQueue.poll();
		return mCurrentProvider;
	}

	public OpenStreetMapAsyncTileProvider getCurrentProvider() {
		return mCurrentProvider;
	}
}

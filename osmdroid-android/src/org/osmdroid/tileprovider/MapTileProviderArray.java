package org.osmdroid.tileprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

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
 *
 */
public class MapTileProviderArray extends MapTileProviderBase {

	private final ConcurrentHashMap<MapTileRequestState, MapTile> mWorking;

	private static final Logger logger = LoggerFactory.getLogger(MapTileProviderArray.class);

	protected final List<MapTileModuleProviderBase> mTileProviderList;

	/**
	 * Creates an {@link MapTileProviderArray} with no tile providers.
	 *
	 * @param aRegisterReceiver
	 *            a {@link IRegisterReceiver}
	 */
	protected MapTileProviderArray(final ITileSource pTileSource,
			final IRegisterReceiver pRegisterReceiver) {
		this(pTileSource, pRegisterReceiver, new MapTileModuleProviderBase[0]);
	}

	/**
	 * Creates an {@link MapTileProviderArray} with the specified tile providers.
	 *
	 * @param aRegisterReceiver
	 *            a {@link IRegisterReceiver}
	 * @param tileProviderArray
	 *            an array of {@link MapTileModuleProviderBase}
	 */
	public MapTileProviderArray(final ITileSource pTileSource,
			final IRegisterReceiver aRegisterReceiver,
			final MapTileModuleProviderBase[] pTileProviderArray) {
		super(pTileSource);

		mWorking = new ConcurrentHashMap<MapTileRequestState, MapTile>();

		mTileProviderList = new ArrayList<MapTileModuleProviderBase>();
		Collections.addAll(mTileProviderList, pTileProviderArray);
	}

	@Override
	public void detach() {
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				tileProvider.detach();
			}
		}
	}

	@Override
	public Drawable getMapTile(final MapTile pTile) {
		final Drawable tile = mTileCache.getMapTile(pTile);
		if (tile != null) {
			if (DEBUGMODE)
				logger.debug("MapTileCache succeeded for: " + pTile);
			return tile;
		} else {
			boolean alreadyInProgress = false;
			synchronized (mWorking) {
				alreadyInProgress = mWorking.containsValue(pTile);
			}

			if (!alreadyInProgress) {
				if (DEBUGMODE)
					logger.debug("Cache failed, trying from async providers: " + pTile);

				MapTileRequestState state;
				synchronized (mTileProviderList) {
					final MapTileModuleProviderBase[] providerArray =
						new MapTileModuleProviderBase[mTileProviderList.size()];
					state = new MapTileRequestState(pTile,
							mTileProviderList.toArray(providerArray), this);
				}

				synchronized (mWorking) {
					// Check again
					alreadyInProgress = mWorking.containsValue(pTile);
					if (alreadyInProgress)
						return null;

					mWorking.put(state, pTile);
				}

				final MapTileModuleProviderBase provider = findNextAppropriateProvider(state);
				if (provider != null)
					provider.loadMapTileAsync(state);
				else
					mapTileRequestFailed(state);
			}
			return null;
		}
	}

	@Override
	public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
		synchronized (mWorking) {
			mWorking.remove(aState);
		}
		super.mapTileRequestCompleted(aState, aDrawable);
	}

	@Override
	public void mapTileRequestFailed(final MapTileRequestState aState) {
		final MapTileModuleProviderBase nextProvider = findNextAppropriateProvider(aState);
		if (nextProvider != null) {
			nextProvider.loadMapTileAsync(aState);
		} else {
			synchronized (mWorking) {
				mWorking.remove(aState);
			}
			super.mapTileRequestFailed(aState);
		}
	}

	/**
	 * We want to not use a provider that doesn't exist anymore in the chain, and we want to not use
	 * a provider that requires a data connection when one is not available.
	 */
	protected MapTileModuleProviderBase findNextAppropriateProvider(final MapTileRequestState aState) {
		MapTileModuleProviderBase provider = null;
		// The logic of the while statement is
		// "Keep looping until you get null, or a provider that still exists and has a data connection if it needs one,"
		do {
			provider = aState.getNextProvider();
		} while ((provider != null)
				&& (!getProviderExists(provider) || (!useDataConnection() && provider
						.getUsesDataConnection())));
		return provider;
	}

	public boolean getProviderExists(final MapTileModuleProviderBase provider) {
		synchronized (mTileProviderList) {
			return mTileProviderList.contains(provider);
		}
	}

	@Override
	public int getMinimumZoomLevel() {
		int result = MAXIMUM_ZOOMLEVEL;
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				if (tileProvider.getMinimumZoomLevel() < result)
					result = tileProvider.getMinimumZoomLevel();
			}
		}
		return result;
	}

	@Override
	public int getMaximumZoomLevel() {
		int result = MINIMUM_ZOOMLEVEL;
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				if (tileProvider.getMaximumZoomLevel() > result)
					result = tileProvider.getMaximumZoomLevel();
			}
		}
		return result;
	}

	@Override
	public void setTileSource(final ITileSource aTileSource) {
		super.setTileSource(aTileSource);

		for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
			tileProvider.setTileSource(aTileSource);
			clearTileCache();
		}
	}
}

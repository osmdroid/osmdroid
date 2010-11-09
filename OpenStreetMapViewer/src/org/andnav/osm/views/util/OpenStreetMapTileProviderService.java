package org.andnav.osm.views.util;

import org.andnav.osm.services.IOpenStreetMapTileProviderService;
import org.andnav.osm.services.IOpenStreetMapTileProviderServiceCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class OpenStreetMapTileProviderService extends OpenStreetMapTileProvider implements ServiceConnection {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileProviderService.class);

	private final Context mContext;

	private IOpenStreetMapTileProviderService mTileService;

	/**
	 * Service is bound, but maybe not still connected.
	 */
	private boolean mServiceBound;

	public OpenStreetMapTileProviderService(final Context pContext, final Handler pDownloadFinishedListener) {
		super(pDownloadFinishedListener);
		mContext = pContext;
		bindToService();
	}

	@Override
	public void onServiceConnected(final ComponentName name, final IBinder service) {
		if (DEBUGMODE)
			logger.debug("onServiceConnected(" + name + ")");

		mTileService = IOpenStreetMapTileProviderService.Stub.asInterface(service);

		try {
			mTileService.setCallback(mServiceCallback);
		} catch (RemoteException e) {
			logger.error("Error setting callback", e);
		}

		try {
			mDownloadFinishedHandler.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);
		} catch(Exception e) {
			logger.error("Error sending success message on connect", e);
		}
	};

	@Override
	public void onServiceDisconnected(final ComponentName name) {
		onDisconnect();
		if (DEBUGMODE)
			logger.debug( "disconnected");
	}

	/**
	 * Get the tile from the cache.
	 * If it's in the cache then it will be returned.
	 * If not it will return null and request it from the service.
	 * In turn, the service will request it from the file system.
	 * If it's found in the file system it will notify the callback.
	 * If not it will initiate a download.
	 * When the download has finished it will notify the callback.
	 * @param aTile the tile being requested
	 * @return the tile bitmap if found in the cache, null otherwise
	 */
	@Override
	public Drawable getMapTile(final OpenStreetMapTile aTile) {
		if (mTileCache.containsTile(aTile)) { 							// from cache
			if (DEBUGMODE)
				logger.debug( "MapTileCache succeeded for: " + aTile);
			return mTileCache.getMapTile(aTile);
		} else { 															// from service
			if (mTileService != null) {
				if (DEBUGMODE)
					logger.debug( "Cache failed, trying from FS: " + aTile);
				try {
					mTileService.requestMapTile(aTile.getRenderer().name(), aTile.getZoomLevel(), aTile.getX(), aTile.getY());
				} catch (Throwable e) {
					logger.error("Error getting map tile from tile service: " + aTile, e);
				}
			} else {
				// try to reconnect, but the connection will take time.
				if (!bindToService()) {
					if (DEBUGMODE)
						logger.debug( "Cache failed, can't get from FS because no tile service: " + aTile);
				} else {
					if (DEBUGMODE)
						logger.debug( "Cache failed, tile service still not woken up: " + aTile);
				}
			}

			return null;
		}
	}

	/***
	 * Disconnects from the tile downloader service.
	 */
	@Override
	public void detach()
	{
		if (mServiceBound)
		{
			if (DEBUGMODE)
				logger.debug( "Unbinding service");
			mContext.unbindService(this);
			onDisconnect();
		}
	}

	private boolean bindToService()
	{
		if (mServiceBound)
			return true;

		boolean success = mContext.bindService(new Intent(IOpenStreetMapTileProviderService.class.getName()), this, Context.BIND_AUTO_CREATE);

		if (!success)
			logger.error("Could not bind to " + IOpenStreetMapTileProviderService.class.getName());

		mServiceBound = success;

		return success;
	}

	private void onDisconnect()
	{
		mServiceBound = false;
		mTileService = null;
	}

	IOpenStreetMapTileProviderServiceCallback mServiceCallback = new IOpenStreetMapTileProviderServiceCallback.Stub() {
		@Override
		public void mapTileRequestCompleted(final String aRendererName, final int aZoomLevel, final int aTileX, final int aTileY, final String aTilePath) throws RemoteException {
	    	// TODO this will go wrong if you use a renderer that the factory doesn't know about
			final IOpenStreetMapRendererInfo renderer = OpenStreetMapRendererFactory.getRenderer(aRendererName);
			final OpenStreetMapTile tile = new OpenStreetMapTile(renderer, aZoomLevel, aTileX, aTileY);
			OpenStreetMapTileProviderService.this.mapTileRequestCompleted(tile, aTilePath);
		}
	};

}

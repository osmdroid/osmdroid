package org.andnav.osm.views.util;

import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.tileprovider.OpenStreetMapTileFilesystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.os.Handler;

public class OpenStreetMapTileProviderDirect extends OpenStreetMapTileProvider implements IOpenStreetMapTileProviderCallback {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileProviderDirect.class);
	
	private final OpenStreetMapTileFilesystemProvider mFileSystemProvider;
	private final String mCloudmadeKey;

	public OpenStreetMapTileProviderDirect(final Handler pDownloadFinishedListener, final String aCloudmadeKey) {
		super(pDownloadFinishedListener);
		mFileSystemProvider = new OpenStreetMapTileFilesystemProvider(this);
		mCloudmadeKey = aCloudmadeKey;
	}

	@Override
	public void detach() {
	}

	@Override
	public Bitmap getMapTile(final OpenStreetMapTile pTile) {
		if (mTileCache.containsTile(pTile)) {
			if (DEBUGMODE)
				logger.debug("MapTileCache succeeded for: " + pTile);
			return mTileCache.getMapTile(pTile);
		} else {
			if (DEBUGMODE)
				logger.debug("Cache failed, trying from FS: " + pTile);
			mFileSystemProvider.loadMapTileAsync(pTile);
			return null;
		}
	}

	@Override
	public String getCloudmadeKey() {
		return mCloudmadeKey;
	}
}

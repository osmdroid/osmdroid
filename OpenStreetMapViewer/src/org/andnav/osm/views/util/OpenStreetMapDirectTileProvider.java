package org.andnav.osm.views.util;

import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.tileprovider.OpenStreetMapTileFilesystemProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class OpenStreetMapDirectTileProvider extends OpenStreetMapTileProvider implements IOpenStreetMapTileProviderCallback {

	private final OpenStreetMapTileFilesystemProvider mFileSystemProvider;

	public OpenStreetMapDirectTileProvider(final Context pContext,
			final Handler pDownloadFinishedListener) {
		super(pContext, pDownloadFinishedListener);
		mFileSystemProvider = new OpenStreetMapTileFilesystemProvider(this);
	}

	@Override
	public void detach() {
	}

	@Override
	public Bitmap getMapTile(final OpenStreetMapTile pTile) {
		if (mTileCache.containsTile(pTile)) {
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "MapTileCache succeeded for: " + pTile);
			return mTileCache.getMapTile(pTile);
		} else {
			if (DEBUGMODE)
				Log.d(DEBUGTAG, "Cache failed, trying from FS: " + pTile);
			mFileSystemProvider.loadMapTileAsync(pTile);
			return null;
		}
	}
}

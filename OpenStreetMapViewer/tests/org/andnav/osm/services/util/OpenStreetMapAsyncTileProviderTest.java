package org.andnav.osm.services.util;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.andnav.osm.services.IOpenStreetMapTileProviderCallback;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Neil Boyd
 *
 */
public class OpenStreetMapAsyncTileProviderTest extends TestCase {

	/**
	 * Test that the tiles are loaded in most recently accessed order.
	 * @throws InterruptedException 
	 */
	public void test_order() throws InterruptedException {
		
		final OpenStreetMapAsyncTileProvider target = new OpenStreetMapAsyncTileProvider(1, 10) {
			@Override
			protected String debugtag() {
				return "OpenStreetMapAsyncTileProviderTest";
			}
			@Override
			protected Runnable getTileLoader(IOpenStreetMapTileProviderCallback aTileProviderCallback) {
				return new TileLoader(aTileProviderCallback) {
					@Override
					protected void loadTile(final OpenStreetMapTile aTile, final TileLoaderCallback aTileLoaderCallback) throws CantContinueException {
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
						aTileLoaderCallback.tileLoaded(aTile, aTile.toString(), true);
					}
				};
			}
		};

		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final IOpenStreetMapTileProviderCallback tileProviderCallback = new IOpenStreetMapTileProviderCallback() {
			@Override
			public void mapTileRequestCompleted(int pRendererID, int pZoomLevel, int pTileX, int pTileY, String pTilePath) throws RemoteException {
				tiles.add(new OpenStreetMapTile(pRendererID, pZoomLevel, pTileX, pTileY));
			}
			@Override
			public IBinder asBinder() {
				return null;
			}
		};
		
		final OpenStreetMapTile tile1 = new OpenStreetMapTile(1, 1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(2, 2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(3, 3, 3, 3);

		// request the three tiles
		target.loadMapTileAsync(tile1, tileProviderCallback);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile2, tileProviderCallback);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile3, tileProviderCallback);
		
		// wait 4 seconds (because it takes 1 second for each tile + an extra second)
		Thread.sleep(4000);
		
		// the tiles should have been loaded in the order 1, 3, 2
		// because 1 was loaded immediately, 2 was next, 
		// but 3 was requested before 2 started, so it jumped the queue		
		assertEquals("tile1 is first", tile1, tiles.get(0));
		assertEquals("tile3 is second", tile3, tiles.get(1));
		assertEquals("tile2 is third", tile2, tiles.get(2));
	}
}

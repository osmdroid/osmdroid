package org.andnav.osm.services.util;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapAsyncTileProvider;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

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
		
		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final IOpenStreetMapTileProviderCallback tileProviderCallback = new IOpenStreetMapTileProviderCallback() {
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile pTile, final String pTilePath) {
				tiles.add(pTile);
			}
		};
		
		final OpenStreetMapAsyncTileProvider target = new OpenStreetMapAsyncTileProvider(tileProviderCallback, 1, 10) {
			@Override
			protected String debugtag() {
				return "OpenStreetMapAsyncTileProviderTest";
			}
			@Override
			protected Runnable getTileLoader() {
				return new TileLoader() {
					@Override
					protected void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
						tileLoaded(aTile, aTile.toString(), true);
					}
				};
			}
		};

		final OpenStreetMapTile tile1 = new OpenStreetMapTile(1, 1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(2, 2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(3, 3, 3, 3);

		// request the three tiles
		target.loadMapTileAsync(tile1);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile2);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile3);
		
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

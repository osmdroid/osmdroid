package org.andnav.osm.tileprovider;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;

import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.junit.Test;

/**
 * @author Neil Boyd
 *
 */
public class OpenStreetMapAsyncTileProviderTest {

	@Test
	public void test_put_twice() {

		final IOpenStreetMapTileProviderCallback tileProviderCallback = new IOpenStreetMapTileProviderCallback() {
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final String aTilePath) {
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final InputStream aTileInputStream) {
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile) {
			}
			@Override
			public String getCloudmadeKey() {
				return "key";
			}
			@Override
			public boolean useDataConnection() {
				return false;
			}
		};

		final OpenStreetMapAsyncTileProvider target = new OpenStreetMapAsyncTileProvider(tileProviderCallback, 1, 10) {
			@Override
			protected String threadGroupName() {
				return "OpenStreetMapAsyncTileProviderTest";
			}
			@Override
			protected Runnable getTileLoader() {
				return new TileLoader() {
					@Override
					protected void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
						// does nothing - doesn't call the callback
					}
				};
			}
		};

		final OpenStreetMapTile tile = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 1, 1, 1);

		// request the same tile twice
		target.loadMapTileAsync(tile);
		target.loadMapTileAsync(tile);

		// check that is only one tile pending
		assertEquals("One tile pending", 1, target.mPending.size());
	}

	/**
	 * Test that the tiles are loaded in most recently accessed order.
	 * @throws InterruptedException
	 */
	@Test
	public void test_order() throws InterruptedException {

		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final IOpenStreetMapTileProviderCallback tileProviderCallback = new IOpenStreetMapTileProviderCallback() {
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final String aTilePath) {
				tiles.add(aTile);
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final InputStream aTileInputStream) {
				tiles.add(aTile);
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile) {
			}
			@Override
			public String getCloudmadeKey() {
				return "key";
			}
			@Override
			public boolean useDataConnection() {
				return false;
			}
		};

		final OpenStreetMapAsyncTileProvider target = new OpenStreetMapAsyncTileProvider(tileProviderCallback, 1, 10) {
			@Override
			protected String threadGroupName() {
				return "OpenStreetMapAsyncTileProviderTest";
			}
			@Override
			protected Runnable getTileLoader() {
				return new TileLoader() {
					@Override
					protected void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
						tileLoaded(aTile, aTile.toString());
					}
				};
			}
		};

		final OpenStreetMapTile tile1 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 3, 3, 3);

		// request the three tiles
		target.loadMapTileAsync(tile1);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile2);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile3);

		// wait 4 seconds (because it takes 1 second for each tile + an extra second)
		Thread.sleep(4000);

		// check that there are three tiles in the list (ie no duplicates)
		assertEquals("Three tiles in the list", 3, tiles.size());

		// the tiles should have been loaded in the order 1, 3, 2
		// because 1 was loaded immediately, 2 was next,
		// but 3 was requested before 2 started, so it jumped the queue
		assertEquals("tile1 is first", tile1, tiles.get(0));
		assertEquals("tile3 is second", tile3, tiles.get(1));
		assertEquals("tile2 is third", tile2, tiles.get(2));
	}

	/**
	 * Test that adding the same tile more than once moves it up the queue.
	 * @throws InterruptedException
	 */
	@Test
	public void test_jump_queue() throws InterruptedException {

		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final IOpenStreetMapTileProviderCallback tileProviderCallback = new IOpenStreetMapTileProviderCallback() {
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final String aTilePath) {
				tiles.add(aTile);
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile, final InputStream aTileInputStream) {
				tiles.add(aTile);
			}
			@Override
			public void mapTileRequestCompleted(final OpenStreetMapTile aTile) {
			}
			@Override
			public String getCloudmadeKey() {
				return "key";
			}
			@Override
			public boolean useDataConnection() {
				return false;
			}
		};

		final OpenStreetMapAsyncTileProvider target = new OpenStreetMapAsyncTileProvider(tileProviderCallback, 1, 10) {
			@Override
			protected String threadGroupName() {
				return "OpenStreetMapAsyncTileProviderTest";
			}
			@Override
			protected Runnable getTileLoader() {
				return new TileLoader() {
					@Override
					protected void loadTile(final OpenStreetMapTile aTile) throws CantContinueException {
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
						tileLoaded(aTile, aTile.toString());
					}
				};
			}
		};

		final OpenStreetMapTile tile1 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(OpenStreetMapRendererFactory.MAPNIK, 3, 3, 3);

		// request tile1, tile2, tile3, then tile2 again
		target.loadMapTileAsync(tile1);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile2);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile3);
		Thread.sleep(100); // give the thread time to run
		target.loadMapTileAsync(tile2);

		// wait 4 seconds (because it takes 1 second for each tile + an extra second)
		Thread.sleep(4000);

		// check that there are three tiles in the list (ie no duplicates)
		assertEquals("Three tiles in the list", 3, tiles.size());

		// the tiles should have been loaded in the order 1, 2, 3
		// 3 jumped ahead of 2, but then 2 jumped ahead of it again
		assertEquals("tile1 is first", tile1, tiles.get(0));
		assertEquals("tile2 is second", tile2, tiles.get(1));
		assertEquals("tile3 is third", tile3, tiles.get(2));
	}
}

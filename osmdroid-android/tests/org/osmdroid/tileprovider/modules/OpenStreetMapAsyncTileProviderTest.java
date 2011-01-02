package org.osmdroid.tileprovider.modules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.osmdroid.tileprovider.IOpenStreetMapTileProviderCallback;
import org.osmdroid.tileprovider.OpenStreetMapTile;
import org.osmdroid.tileprovider.OpenStreetMapTileRequestState;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;

/**
 * @author Neil Boyd
 * 
 */
public class OpenStreetMapAsyncTileProviderTest {

	final IOpenStreetMapTileProviderCallback mTileProviderCallback = new IOpenStreetMapTileProviderCallback() {

		@Override
		public void mapTileRequestCompleted(final OpenStreetMapTileRequestState aState,
				final Drawable aDrawable) {
		}

		@Override
		public void mapTileRequestCandidate(final OpenStreetMapTileRequestState aState,
				final Drawable aDrawable) {
		}

		@Override
		public void mapTileRequestFailed(final OpenStreetMapTileRequestState aState) {
		}

		@Override
		public boolean useDataConnection() {
			return false;
		}
	};

	final OpenStreetMapTileModuleProviderBase mTileProvider = new OpenStreetMapTileModuleProviderBase(
			1, 10) {
		@Override
		protected String getThreadGroupName() {
			return "OpenStreetMapAsyncTileProviderTest";
		}

		@Override
		protected Runnable getTileLoader() {
			return new TileLoader() {
				@Override
				protected Drawable loadTile(final OpenStreetMapTileRequestState aState)
						throws CantContinueException {
					// does nothing - doesn't call the callback
					return null;
				}
			};
		}

		@Override
		public boolean getUsesDataConnection() {
			return false;
		}

		@Override
		public int getMinimumZoomLevel() {
			return 0;
		}

		@Override
		public int getMaximumZoomLevel() {
			return 0;
		}

		@Override
		protected String getName() {
			return "test";
		}

		@Override
		public void setTileSource(final ITileSource pTileSource) {
			// Do nothing
		}
	};

	@Test
	public void test_put_twice() {

		final OpenStreetMapTile tile = new OpenStreetMapTile(1, 1, 1);

		// request the same tile twice
		final OpenStreetMapTileRequestState state = new OpenStreetMapTileRequestState(tile,
				new OpenStreetMapTileDownloader[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state);
		mTileProvider.loadMapTileAsync(state);

		// check that is only one tile pending
		assertEquals("One tile pending", 1, mTileProvider.mPending.size());
	}

	/**
	 * Test that the tiles are loaded in most recently accessed order.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test_order() throws InterruptedException {

		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final OpenStreetMapTile tile1 = new OpenStreetMapTile(1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(3, 3, 3);

		// request the three tiles
		final OpenStreetMapTileRequestState state1 = new OpenStreetMapTileRequestState(tile1,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state1);
		Thread.sleep(100); // give the thread time to run
		final OpenStreetMapTileRequestState state2 = new OpenStreetMapTileRequestState(tile2,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state2);
		Thread.sleep(100); // give the thread time to run
		final OpenStreetMapTileRequestState state3 = new OpenStreetMapTileRequestState(tile3,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state3);

		// wait 4 seconds (because it takes 1 second for each tile + an extra
		// second)
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
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test_jump_queue() throws InterruptedException {

		final ArrayList<OpenStreetMapTile> tiles = new ArrayList<OpenStreetMapTile>();

		final OpenStreetMapTile tile1 = new OpenStreetMapTile(1, 1, 1);
		final OpenStreetMapTile tile2 = new OpenStreetMapTile(2, 2, 2);
		final OpenStreetMapTile tile3 = new OpenStreetMapTile(3, 3, 3);

		// request tile1, tile2, tile3, then tile2 again
		final OpenStreetMapTileRequestState state1 = new OpenStreetMapTileRequestState(tile1,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state1);
		Thread.sleep(100); // give the thread time to run
		final OpenStreetMapTileRequestState state2 = new OpenStreetMapTileRequestState(tile2,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state2);
		Thread.sleep(100); // give the thread time to run
		final OpenStreetMapTileRequestState state3 = new OpenStreetMapTileRequestState(tile3,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state3);
		Thread.sleep(100); // give the thread time to run
		final OpenStreetMapTileRequestState state4 = new OpenStreetMapTileRequestState(tile2,
				new OpenStreetMapTileModuleProviderBase[] {}, mTileProviderCallback);
		mTileProvider.loadMapTileAsync(state4);

		// wait 4 seconds (because it takes 1 second for each tile + an extra
		// second)
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

package org.osmdroid.bonuspack.mapsforge;

import java.io.File;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import android.graphics.drawable.Drawable;

/**
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileModuleProvider extends MapTileFileStorageProviderBase {

	protected MapsForgeTileSource tileSource;

	/**
	 * Constructor
	 * 
	 * @param receiverRegistrar
	 * @param file
	 * @param tileSource
	 */
	public MapsForgeTileModuleProvider(IRegisterReceiver receiverRegistrar, File file, MapsForgeTileSource tileSource) {

		super(receiverRegistrar, OpenStreetMapTileProviderConstants.NUMBER_OF_TILE_FILESYSTEM_THREADS, OpenStreetMapTileProviderConstants.TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

		this.tileSource = tileSource;

	}

	@Override
	protected String getName() {
		return "MapsforgeTiles Provider";
	}

	@Override
	protected String getThreadGroupName() {
		return "mapsforgetilesprovider";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	}

	@Override
	public boolean getUsesDataConnection() {
		return false;
	}

	@Override
	public int getMinimumZoomLevel() {
		return tileSource.getMinimumZoomLevel();
	}

	@Override
	public int getMaximumZoomLevel() {
		return tileSource.getMaximumZoomLevel();
	}

	@Override
	public void setTileSource(ITileSource tileSource) {
		//prevent re-assignment of tile source
		if (tileSource instanceof MapsForgeTileSource) {
			this.tileSource = (MapsForgeTileSource) tileSource;
		}
	}

	private class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState pState) {
			return tileSource.renderTile(pState.getMapTile());
		}
	}

}

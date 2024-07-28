package org.osmdroid.mapsforge;


import android.content.Context;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.renderer.DirectRenderer;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.util.MapTileIndex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This lets you hook up multiple MapsForge files, it will render to the screen the first
 * image that's available.
 * <p>
 * Adapted from code from here: <a href="https://github.com/MKergall/osmbonuspack">...</a>, which is LGPL
 * <a href="http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/">...</a>
 *
 * @author Salida Software
 * Adapted from <a href="code">found here : http://www.sieswerda.net/2012/08/15/upping-the-devel</a>oper-friendliness/
 */
public class MapsForgeTileProvider extends MapTileProviderArray {
    IFilesystemCache tileWriter;

    public MapsForgeTileProvider(@NonNull final Context context, IRegisterReceiver pRegisterReceiver, MapsForgeTileSource pTileSource, IFilesystemCache cacheWriter) {
        super(context, pTileSource, pRegisterReceiver);

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(context, pRegisterReceiver, pTileSource);
        mTileProviderList.add(fileSystemProvider);

        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(context, pRegisterReceiver, pTileSource);
        mTileProviderList.add(archiveProvider);


        if (cacheWriter != null) {
            tileWriter = cacheWriter;
        } else {
            tileWriter = new SqlTileWriter();
        }

        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the map file.
        MapsForgeTileModuleProvider moduleProvider = new MapsForgeTileModuleProvider(context, pRegisterReceiver, (MapsForgeTileSource) getTileSource(), tileWriter);
        //this is detached by super


        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        mTileProviderList.add(moduleProvider);

        // In mapsforge the tiles bitmap may need to be refreshed according to neighboring tiles' labels
        pTileSource.addTileRefresher(new DirectRenderer.TileRefresher() {
            @Override
            public void refresh(final Tile pTile) {
                final long index = MapTileIndex.getTileIndex(pTile.zoomLevel, pTile.tileX, pTile.tileY);
                expireInMemoryCache(index);
            }
        });
    }

    @Override
    public void onDetach(@Nullable final Context context) {
        if (tileWriter != null)
            tileWriter.onDetach(context);
        tileWriter = null;
        super.onDetach(context);
    }

}
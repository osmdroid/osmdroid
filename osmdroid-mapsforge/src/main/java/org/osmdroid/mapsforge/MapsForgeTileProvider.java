package org.osmdroid.mapsforge;


import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.TileWriter;

/**
 * This lets you hook up multiple MapsForge files, it will render to the screen the first
 * image that's available.
 *
 * Adapted from code from here: https://github.com/MKergall/osmbonuspack, which is LGPL
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileProvider extends MapTileProviderArray {

    /**
     *
     * @param pRegisterReceiver
     */
    public MapsForgeTileProvider(IRegisterReceiver pRegisterReceiver, MapsForgeTileSource pTileSource) {
        super(pTileSource, pRegisterReceiver);

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                pRegisterReceiver, pTileSource);
        mTileProviderList.add(fileSystemProvider);

        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
                pRegisterReceiver, pTileSource);
        mTileProviderList.add(archiveProvider);

        //this is our local sqlite cache
        IFilesystemCache writer = new TileWriter();

        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the map file.
        MapsForgeTileModuleProvider moduleProvider = new MapsForgeTileModuleProvider(pRegisterReceiver, (MapsForgeTileSource) getTileSource(), writer);

        //TODO wire in cache provider to speed up performance

        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        mTileProviderList.add(moduleProvider);

    }

}
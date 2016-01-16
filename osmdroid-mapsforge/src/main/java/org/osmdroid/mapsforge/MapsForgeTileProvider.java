package org.osmdroid.mapsforge;


import java.io.File;
import java.util.Collections;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

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

    public MapsForgeTileProvider(IRegisterReceiver receiverRegistrar, File[] files) {

        super(MapsForgeTileSource.createFromFile(files), receiverRegistrar);

        //TODO add hooks for setting rendering theme and some of the many other features MapsForge provides

        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the map file.
        MapsForgeTileModuleProvider moduleProvider = new MapsForgeTileModuleProvider(receiverRegistrar, (MapsForgeTileSource) getTileSource());

        MapTileModuleProviderBase[] pTileProviderArray = new MapTileModuleProviderBase[] { moduleProvider };

        //TODO wire in cache provider to speed up performance

        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        Collections.addAll(mTileProviderList, pTileProviderArray);

    }

}
package org.osmdroid.bonuspack.mapsforge;

import java.io.File;
import java.util.Collections;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

/**
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileProvider extends MapTileProviderArray {

    public MapsForgeTileProvider(IRegisterReceiver receiverRegistrar, File file) {

        super(MapsForgeTileSource.createFromFile(file), receiverRegistrar);

        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the map file.
        MapsForgeTileModuleProvider moduleProvider;
        moduleProvider = new MapsForgeTileModuleProvider(receiverRegistrar, file, (MapsForgeTileSource) getTileSource());

        MapTileModuleProviderBase[] pTileProviderArray;
        pTileProviderArray = new MapTileModuleProviderBase[] { moduleProvider };

        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        Collections.addAll(mTileProviderList, pTileProviderArray);
    }
    
}

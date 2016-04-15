package org.osmdroid.forge.app;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.RemoteException;
import android.test.AndroidTestCase;

import org.osmdroid.mapsforge.MapsForgeTileModuleProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Borrowed from the osmdroid-android-it test classes by Neil Boyd
 * <p/>
 * assumes that the sample world.map is copied to external storage/osmdroid/
 *
 * @author Alex O'Ree
 */
public class MapsForgeMapTileProviderDirectTest extends AndroidTestCase {

    MapsForgeTileModuleProvider mProvider;
    MapsForgeTileSource source;
    TileWriter writer = new TileWriter();

    @Override
    protected void setUp() throws Exception {


        Set<File> mapfiles = MainActivity.findMapFiles();
        //do a simple scan of local storage for .map files.
        File[] maps = new File[mapfiles.size()];
        maps = mapfiles.toArray(maps);
        source = MapsForgeTileSource.createFromFiles(maps);
        mProvider = new MapsForgeTileModuleProvider(new SimpleRegisterReceiver(getContext()),
                source, writer);

        super.setUp();
    }


    public void test_getMapTile_found() throws RemoteException, FileNotFoundException, BitmapTileSourceBase.LowMemoryException, IOException {

        OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS=true;
        for (int i = 3; i < 22; i++) {
            final MapTile tile = new MapTile(i, 3, 4);

            BitmapDrawable drawable = (BitmapDrawable) source.renderTile(tile);
            assertNotNull(tile.toString() + " failed", drawable);


            //new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getPath() + "/" + tile.toString()).mkdirs();

            //FileOutputStream fos = new FileOutputStream(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getPath() + "/" + tile.toString() + ".png");
            //((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
            //fos.close();

        }

    }
}

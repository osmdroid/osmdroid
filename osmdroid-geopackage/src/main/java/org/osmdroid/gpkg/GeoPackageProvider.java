package org.osmdroid.gpkg;

import android.content.Context;
import android.os.Build;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;

/**
 * GeoPackage +
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class GeoPackageProvider extends MapTileProviderArray implements IMapTileProviderCallback {

    protected GeoPackageMapTileModuleProvider geopackage;
    protected IFilesystemCache tileWriter;

    public GeoPackageProvider(File[] db, Context context) {
        this(new SimpleRegisterReceiver(context), new NetworkAvailabliltyCheck(context),
            TileSourceFactory.DEFAULT_TILE_SOURCE, context, null, db);
    }


    public GeoPackageProvider(final IRegisterReceiver pRegisterReceiver,
                              final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
                              final Context pContext, final IFilesystemCache cacheWriter, File[] databases) {


        super(pTileSource, pRegisterReceiver);


        if (cacheWriter != null) {
            tileWriter = cacheWriter;
        } else {
            if (Build.VERSION.SDK_INT < 10) {
                tileWriter = new TileWriter();
            } else {
                tileWriter = new SqlTileWriter();
            }
        }

        if (Build.VERSION.SDK_INT < 10) {
            final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                pRegisterReceiver, pTileSource);
            mTileProviderList.add(fileSystemProvider);
        } else {
            final MapTileSqlCacheProvider cachedProvider = new MapTileSqlCacheProvider(pRegisterReceiver, pTileSource);
            mTileProviderList.add(cachedProvider);
        }
        geopackage = new GeoPackageMapTileModuleProvider(databases, pContext, tileWriter);
        mTileProviderList.add(geopackage);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
            aNetworkAvailablityCheck);
        mTileProviderList.add(downloaderProvider);
    }

    public GeoPackageMapTileModuleProvider geoPackageMapTileModuleProvider(){
        return geopackage;
    }


    @Override
    public IFilesystemCache getTileWriter() {
        return tileWriter;
    }

    @Override
    public void detach() {
        //https://github.com/osmdroid/osmdroid/issues/213
        //close the writer
        if (tileWriter != null)
            tileWriter.onDetach();
        tileWriter = null;
        super.detach();
    }

    public static ITileSource getTileSource(String database, String table) {
        return new XYTileSource(table, 0, 22, 256, "jpg", new String[]{database});
    }
}

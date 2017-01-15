package org.osmdroid.gpkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * This is called the GeoPackage Slowmo because it's damn slow.
 * Created by alex on 10/29/15.
 */
public class GeoPackageMapTileModuleProvider extends MapTileModuleProviderBase {

    //TileRetriever retriever;
    IFilesystemCache tileWriter = null;
    GeoPackageManager manager;

    //GeoPackage geoPackage;
    List<String> tiles;
    ITileSource currentTileSource;
    Set<GeoPackage> tileSources = new HashSet<>();
    /**
     * Compress format
     */
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;




    public GeoPackageMapTileModuleProvider(File[] pFile,
                                           final Context context, IFilesystemCache cache) {
        //int pThreadPoolSize, final int pPendingQueueSize
        super(Configuration.getInstance().getTileFileSystemThreads(), Configuration.getInstance().getTileFileSystemMaxQueueSize());

        tileWriter = cache;
        // Get a manager
        manager = GeoPackageFactory.getManager(context);
        // Available databases


        // Import database
        for (int i = 0; i < pFile.length; i++) {
            try {
                boolean imported = manager.importGeoPackage((pFile[i]));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Available databases
        List<String> databases = manager.databases();
        // Open database
        for (int i = 0; i < databases.size(); i++) {
            tileSources.add(manager.open(databases.get(i)));
        }

    }


    public Drawable getMapTile(MapTile pTile) {


        Drawable tile = null;


        String src = currentTileSource.name();  //table name
        //String database = currentTileSource.getBaseUrls()[0]; //database name

        GeoPackage next = null;
        boolean found = false;
        //find out db connection
        Iterator<GeoPackage> iterator = tileSources.iterator();
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next.getTileTables().contains(src)) {
                found = true;
                break;
            }
        }


        if (found) {
            TileDao tileDao = next.getTileDao(src);
            GeoPackageTileRetriever retriever = new GeoPackageTileRetriever(tileDao);

            int zoom = pTile.getZoomLevel();
            int x = pTile.getX();
            int y = pTile.getY();


            GeoPackageTile geoPackageTile = retriever.getTile(x, y, zoom);
            if (geoPackageTile != null && geoPackageTile.data != null) {
                byte[] image = geoPackageTile.data;
                if (image != null) {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.outHeight = 256; //360
                    opt.outWidth = 256;//248
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, opt);
                    tile = new BitmapDrawable(imageBitmap);
                /*Date dateExpires;
                Long override=Configuration.getInstance().getExpirationOverrideDuration();
                if (override!=null) {
                    dateExpires= new Date(System.currentTimeMillis() + override);
                } else {
                    dateExpires = new Date(System.currentTimeMillis() + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + Configuration.getInstance().getExpirationExtendedDuration());
                }
                pTile.setExpires(dateExpires);
                tileWriter.saveFile(src, pTile, new ByteArrayInputStream(image));
                */
                }
            }
        }

        return tile;

    }

    public static class Container {
        public String database;
        public List<String> tiles;
        public List<String> features;
    }


    public List<Container> getTileSources() {
        List<Container> srcs = new ArrayList<>();
        List<String> databases = manager.databases();
        for (int i = 0; i < databases.size(); i++) {
            GeoPackage handle = manager.open(databases.get(i));
            Container c = new Container();
            c.database = databases.get(i);
            c.tiles = new ArrayList<>();
            c.tiles.addAll(handle.getTileTables());
            c.features = new ArrayList<>();
            c.features.addAll(handle.getFeatureTables());
            srcs.add(c);
        }

        return srcs;
    }

    @Override
    public void detach() {


        if (tileSources!=null) {
            Iterator<GeoPackage> iterator = tileSources.iterator();
            while (iterator.hasNext()) {
                iterator.next().close();
            }
            tileSources.clear();
        }
        manager = null;
    }


    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) {

            final MapTile pTile = pState.getMapTile();

            try {
                Drawable mapTile = getMapTile(pTile);
                return mapTile;
            } catch (final Throwable e) {
                Log.e(IMapView.LOGTAG, "Error loading tile", e);
            } finally {
            }

            return null;
        }
    }

    @Override
    protected String getName() {
        return "Geopackage";
    }

    @Override
    protected String getThreadGroupName() {
        return getName();
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
        return 0;
    }

    @Override
    public int getMaximumZoomLevel() {
        return 22;
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        currentTileSource = tileSource;
    }


}

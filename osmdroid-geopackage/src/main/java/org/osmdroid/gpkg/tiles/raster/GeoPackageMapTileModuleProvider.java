package org.osmdroid.gpkg.tiles.raster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.TileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Geopackage raster tile provider
 * Created by alex on 10/29/15.
 */
public class GeoPackageMapTileModuleProvider extends MapTileModuleProviderBase {

    private final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();

    //TileRetriever retriever;
    protected IFilesystemCache tileWriter = null;
    protected GeoPackageManager manager;

    protected GeopackageRasterTileSource currentTileSource;
    protected Set<GeoPackage> tileSources = new HashSet<>();

    public GeoPackageMapTileModuleProvider(File[] pFile,
                                           final Context context, IFilesystemCache cache) {
        //int pThreadPoolSize, final int pPendingQueueSize
        super(Configuration.getInstance().getTileFileSystemThreads(), Configuration.getInstance().getTileFileSystemMaxQueueSize());
        Log.i(IMapView.LOGTAG, "Geopackage support is BETA. Please report any issues");
        tileWriter = cache;
        // Get a manager
        manager = GeoPackageFactory.getManager(context);
        // Available databases


        // Import database
        for (int i = 0; i < pFile.length; i++) {
            try {
                manager.importGeoPackage((pFile[i]));
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


    public Drawable getMapTile(final long pMapTileIndex) {
        Drawable tile = null;

        String database = currentTileSource.getDatabase();
        String table = currentTileSource.getTableDao();
        GeoPackage next = manager.open(database);

        TileDao tileDao = next.getTileDao(table);
        GeoPackageTileRetriever retriever = new GeoPackageTileRetriever(tileDao);

        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);


        GeoPackageTile geoPackageTile = retriever.getTile(x, y, zoom);
        if (geoPackageTile != null && geoPackageTile.data != null) {
            byte[] image = geoPackageTile.data;
            if (image != null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.outHeight = 256; //360
                opt.outWidth = 256;//248
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, opt);
                tile = new BitmapDrawable(imageBitmap);

            }
        }
        next.close();

        return tile;

    }


    /**
     * returns ALL available raster tile sources for all "imported" geopackage databases
     *
     * @return
     */
    public List<GeopackageRasterTileSource> getTileSources() {
        List<GeopackageRasterTileSource> srcs = new ArrayList<>();

        List<String> databases = manager.databases();
        for (int i = 0; i < databases.size(); i++) {

            GeoPackage open = manager.open(databases.get(i));
            List<String> tileTables = open.getTileTables();
            for (int k = 0; k < tileTables.size(); k++) {
                TileDao tileDao = open.getTileDao(tileTables.get(k));

                ProjectionTransform transform = tileDao.getProjection().getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                mil.nga.geopackage.BoundingBox boundingBox = transform.transform(tileDao.getBoundingBox());
                BoundingBox bounds = new BoundingBox(Math.min(tileSystem.getMaxLatitude(), boundingBox.getMaxLatitude()),
                        boundingBox.getMaxLongitude(),
                        Math.max(tileSystem.getMinLatitude(), boundingBox.getMinLatitude()),
                        boundingBox.getMinLongitude());

                srcs.add(new GeopackageRasterTileSource(databases.get(i), tileTables.get(k), (int) tileDao.getMinZoom(), (int) tileDao.getMaxZoom(), bounds));
            }
            open.close();
        }

        return srcs;
    }

    /**
     * returns ALL available raster tile sources for the specified database.
     * This will throw if the database doesn't exist or isn't registered
     *
     * @return
     */
    public List<GeopackageRasterTileSource> getTileSources(String database) {
        List<GeopackageRasterTileSource> srcs = new ArrayList<>();

        GeoPackage open = manager.open(database);
        List<String> tileTables = open.getTileTables();
        for (int k = 0; k < tileTables.size(); k++) {
            TileDao tileDao = open.getTileDao(tileTables.get(k));

            ProjectionTransform transform = tileDao.getProjection().getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            mil.nga.geopackage.BoundingBox boundingBox = transform.transform(tileDao.getBoundingBox());

            BoundingBox bounds = new BoundingBox(Math.min(tileSystem.getMaxLatitude(), boundingBox.getMaxLatitude()),
                    boundingBox.getMaxLongitude(),
                    Math.max(tileSystem.getMinLatitude(), boundingBox.getMinLatitude()),
                    boundingBox.getMinLongitude());
            srcs.add(new GeopackageRasterTileSource(database, tileTables.get(k), (int) tileDao.getMinZoom(), (int) tileDao.getMaxZoom(), bounds));

        }
        open.close();

        return srcs;
    }

    @Override
    public void detach() {


        if (tileSources != null) {
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
        public Drawable loadTile(final long pMapTileIndex) {
            try {
                Drawable mapTile = getMapTile(pMapTileIndex);
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
    public TileLoader getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        if (currentTileSource != null)
            return currentTileSource.getMinimumZoomLevel();
        return 0;
    }

    @Override
    public int getMaximumZoomLevel() {
        if (currentTileSource != null)
            return currentTileSource.getMaximumZoomLevel();
        return 22;
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        if (tileSource instanceof GeopackageRasterTileSource)
            currentTileSource = (GeopackageRasterTileSource) tileSource;

    }


}

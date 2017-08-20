package org.osmdroid.gpkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileLooper;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.features.FeatureTiles;
//import mil.nga.geopackage.tiles.features.MapFeatureTiles;
//import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
//import mil.nga.geopackage.tiles.overlay.FeatureOverlay;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

public class GeopackageFeatureTilesOverlay extends TilesOverlay {
    GeoPackageManager manager;
    Context ctx;
    List<String> databases;
    GeoPackageFeatureTileProvider provider;

    public GeopackageFeatureTilesOverlay(GeoPackageFeatureTileProvider provider, final Context pContext, File[] geopackges) {
        super(provider, pContext);
        this.ctx=pContext;

        this.provider=provider;
// Get a manager
         manager = GeoPackageFactory.getManager(pContext);


// Import database, if needed
        for (File f : geopackges) {
            try {
                boolean imported = manager.importGeoPackage(f);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

// Available databases
        databases = manager.databases();




    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<String> getFeatureTable(String database) throws Exception{
        GeoPackage open = null;
        List<String> featureTables=new ArrayList<>();
        try {
             open = manager.open(database);
            featureTables = open.getFeatureTables();
        }catch (Exception ex) {
            throw ex;
        } finally {
            if (open!=null)
            open.close();
        }

        return featureTables;
    }
    private GeoPackage geoPackage=null;
    private FeatureDao featureDao=null;
    private FeatureTiles featureTiles=null;

    public void setDatabaseAndFeatureTable(String database, String featureTable){
        if (featureDao!=null)
            featureDao=null;
        if (geoPackage!=null){

            geoPackage.close();
            geoPackage=null;
        }
        geoPackage = manager.open(database);
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);


// Index Features
        FeatureIndexManager indexer = new FeatureIndexManager(ctx, geoPackage, featureDao);
        indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
        int indexedCount = indexer.index();
                // Draw tiles from features
        featureTiles = new mil.nga.geopackage.tiles.features.DefaultFeatureTiles(ctx, featureDao);
        featureTiles.setMaxFeaturesPerTile(1000); // Set max features to draw per tile
        NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile(ctx); // Custom feature tile implementation
        featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile); // Draw feature count tiles when max features passed
        featureTiles.setIndexManager(indexer); // Set index manager to query feature indices

        provider.set(featureDao.getZoomLevel(),featureTiles);
    }



    @Override
    public void onDetach(final MapView pMapView) {
        super.onDetach(pMapView);
        if (geoPackage!=null){

            geoPackage.close();
            geoPackage=null;
        }

        featureDao=null;
         featureTiles=null;

    }


}

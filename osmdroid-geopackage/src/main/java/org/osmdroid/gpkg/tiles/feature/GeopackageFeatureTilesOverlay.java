package org.osmdroid.gpkg.tiles.feature;

import android.content.Context;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;

/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

public class GeopackageFeatureTilesOverlay extends TilesOverlay {
    protected GeoPackageManager manager;
    protected Context ctx;
    protected List<String> databases;
    protected GeoPackageFeatureTileProvider provider;
    protected GeoPackage geoPackage = null;
    protected FeatureDao featureDao = null;
    protected FeatureTiles featureTiles = null;

    public GeopackageFeatureTilesOverlay(GeoPackageFeatureTileProvider provider, final Context pContext) {
        super(provider, pContext);
        Log.i(IMapView.LOGTAG, "Geopackage support is BETA. Please report any issues");
        this.ctx = pContext;

        this.provider = provider;
        // Get a manager
        manager = GeoPackageFactory.getManager(pContext);

        // Available databases
        databases = manager.databases();
    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<String> getFeatureTable(String database) throws Exception {
        GeoPackage open = null;
        List<String> featureTables = new ArrayList<>();
        try {
            open = manager.open(database);
            featureTables = open.getFeatureTables();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (open != null)
                open.close();
        }

        return featureTables;
    }


    public void setDatabaseAndFeatureTable(String database, String featureTable) {
        if (featureDao != null)
            featureDao = null;
        if (geoPackage != null) {

            geoPackage.close();
            geoPackage = null;
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

        provider.set(featureDao.getZoomLevel(), featureTiles);
    }


    @Override
    public void onDetach(final MapView pMapView) {
        super.onDetach(pMapView);
        if (geoPackage != null) {

            geoPackage.close();
            geoPackage = null;
        }

        featureDao = null;
        featureTiles = null;

    }


}

package org.osmdroid.gpkg.tiles.raster;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;

/**
 * created on 9/3/2017.
 *
 * @author Alex O'Ree
 */

public class GeopackageRasterTileSource extends XYTileSource {
    private String database;
    private String tableDao;
    private BoundingBox bounds;

    public GeopackageRasterTileSource(String database, String table, int aZoomMinLevel, int aZoomMaxLevel, BoundingBox bbox) {
        super(database + ":" + table, aZoomMinLevel, aZoomMaxLevel, 256, "png", new String[]{""});
        Log.i(IMapView.LOGTAG, "Geopackage support is BETA. Please report any issues");
        this.database = database;
        this.tableDao = table;
        this.bounds = bbox;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableDao() {
        return tableDao;
    }

    public void setTableDao(String tableDao) {
        this.tableDao = tableDao;
    }
}

package org.osmdroid.gpkg.tiles.feature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import mil.nga.geopackage.tiles.features.FeatureTiles;

/**
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 */

public class GeoPackageFeatureTileProvider extends MapTileProviderBase {

    protected IFilesystemCache tileWriter;
    protected int minzoom = 0;
    protected FeatureTiles featureTiles = null;

    public GeoPackageFeatureTileProvider(@NonNull final Context context, @NonNull final ITileSource pTileSource) {
        super(context, pTileSource);

        Log.i(IMapView.LOGTAG, "Geopackage support is BETA. Please report any issues");
        tileWriter = new SqlTileWriter();
    }


    @Override
    public Drawable getMapTile(final long pMapTileIndex) {
        if (featureTiles != null) {
            Bitmap tile = featureTiles.drawTile(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex));
            if (tile != null) {
                Drawable d = new BitmapDrawable(tile);
                return d;
            }
        }
        return null;

    }

    @Override
    public int getMinimumZoomLevel() {
        return minzoom;
    }

    @Override
    public int getMaximumZoomLevel() {
        return 22;
    }

    @Override
    public IFilesystemCache getTileWriter() {
        return tileWriter;
    }

    @Override
    public long getQueueSize() {
        return 0;
    }

    public void set(int minZoom, FeatureTiles featureTiles) {
        this.featureTiles = featureTiles;
        minzoom = minZoom;
    }

    @Override
    public void onDetach(@Nullable final Context context) {
        featureTiles = null;
        super.onDetach(context);
    }
}
package org.osmdroid.tileprovider;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileDownloaderProvider;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider;
import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IMapTileProviderCallback extends IViewBoundingBoxChangedListener {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ TILEPROVIDERTYPE_NONE, TILEPROVIDERTYPE_APPROXIMATER, TILEPROVIDERTYPE_ASSET, TILEPROVIDERTYPE_FILE_ARCHIVE, TILEPROVIDERTYPE_FILE_SYSTEM, TILEPROVIDERTYPE_DOWNLOADER,
            TILEPROVIDERTYPE_SQL_CACHE, TILEPROVIDERTYPE_GEO_PACKAGE_FILE, TILEPROVIDERTYPE_MAPSFORGE })
    @interface TILEPROVIDERTYPE {}
    int TILEPROVIDERTYPE_NONE               = 1 << 23;  //<-- usually set when NO ANY Tile was found
    int TILEPROVIDERTYPE_APPROXIMATER       = 1 << 24;
    int TILEPROVIDERTYPE_ASSET              = 1 << 25;
    int TILEPROVIDERTYPE_FILE_ARCHIVE       = 1 << 26;
    int TILEPROVIDERTYPE_FILE_SYSTEM        = 1 << 27;
    int TILEPROVIDERTYPE_DOWNLOADER         = 1 << 28;
    int TILEPROVIDERTYPE_GEO_PACKAGE_FILE   = 1 << 29;
    int TILEPROVIDERTYPE_SQL_CACHE          = 1 << 30;
    int TILEPROVIDERTYPE_MAPSFORGE          = 1 << 31;
    static String decodeTileProviderName(@TILEPROVIDERTYPE final int providerType) {
        switch (providerType) {
            case TILEPROVIDERTYPE_NONE              : return "NONE";
            case TILEPROVIDERTYPE_APPROXIMATER      : return MapTileApproximater.CONST_MAPTILEPROVIDER_APPROXIMATER.toUpperCase();
            case TILEPROVIDERTYPE_ASSET             : return MapTileAssetsProvider.CONST_MAPTILEPROVIDER_ASSETS.toUpperCase();
            case TILEPROVIDERTYPE_FILE_ARCHIVE      : return MapTileFileArchiveProvider.CONST_MAPTILEPROVIDER_FILEARCHIVE.toUpperCase();
            case TILEPROVIDERTYPE_FILE_SYSTEM       : return MapTileFilesystemProvider.CONST_MAPTILEPROVIDER_FILESISTEM.toUpperCase();
            case TILEPROVIDERTYPE_DOWNLOADER        : return MapTileDownloaderProvider.CONST_MAPTILEPROVIDER_DOWNLOADER.toUpperCase();
            case TILEPROVIDERTYPE_GEO_PACKAGE_FILE  : return "Geopackage".toUpperCase();
            case TILEPROVIDERTYPE_MAPSFORGE         : return "mapsforgetilesprovider".toUpperCase();
            case TILEPROVIDERTYPE_SQL_CACHE         : return MapTileSqlCacheProvider.CONST_MAPTILEPROVIDER_SQLCACHE.toUpperCase();
            default:
                return "UNKNOWN";
        }
    }

    /**
     * The map tile request has started.
     *
     * @param aState    a state object
     * @param pending currently pending Tiles
     * @param working currently Tiles in download/load
     */
    void mapTileRequestStarted(@NonNull MapTileRequestState aState, int pending, int working);

    /**
     * The map tile request has completed.
     *
     * @param aState    a state object
     * @param aDrawable a drawable
     */
    void mapTileRequestCompleted(@NonNull MapTileRequestState aState, Drawable aDrawable);

    /**
     * The map tile request has failed.
     *
     * @param aState a state object
     */
    void mapTileRequestFailed(@NonNull MapTileRequestState aState);

    /**
     * The map tile request has failed - exceeds MaxQueueSize.
     *
     * @param aState a state object
     */
    void mapTileRequestFailedExceedsMaxQueueSize(@NonNull MapTileRequestState aState);

    /**
     * The map tile request has produced an expired tile.
     *
     * @param aState a state object
     */
    void mapTileRequestExpiredTile(@NonNull MapTileRequestState aState, Drawable aDrawable);

    /**
     * The map tile request has been completed but Tile status is <i>unknown</i> or <i>not resolved</i>
     *
     * @param aState a state object
     */
    void mapTileRequestDoneButUnknown(@NonNull MapTileRequestState aState);

    /**
     * The map tile request has been discarted because its coords are outside current {@link org.osmdroid.views.MapView} view boundaries
     *
     * @param aState a state object
     */
    void mapTileRequestDiscartedDueToOutOfViewBounds(@NonNull MapTileRequestState aState);

    /**
     * Returns true if the network connection should be used, false if not.
     *
     * @return true if data connection should be used, false otherwise
     */
    boolean useDataConnection();

    /** {@inheritDoc} */
    @Override
    void onViewBoundingBoxChanged(@NonNull Rect fromBounds, int fromZoom, @NonNull Rect toBounds, int toZoom);
}

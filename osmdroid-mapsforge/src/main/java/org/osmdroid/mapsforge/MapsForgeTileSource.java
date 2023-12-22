package org.osmdroid.mapsforge;

import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidTileBitmap;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.hills.HillsRenderConfig;
import org.mapsforge.map.layer.renderer.DirectRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

import java.io.File;

/**
 * Adapted from code from here: https://github.com/MKergall/osmbonuspack, which is LGPL
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 *
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileSource extends BitmapTileSourceBase {

    // Reasonable defaults ..
    public static int MIN_ZOOM = 3;
    public static int MAX_ZOOM = 29;
    public static final int TILE_SIZE_PIXELS = 256;
    private final DisplayModel model = new DisplayModel();
    private final float scale = DisplayModel.getDefaultUserScaleFactor();
    private RenderThemeFuture theme = null;
    private XmlRenderTheme mXmlRenderTheme = null;
    private DirectRenderer renderer;
    private HillsRenderConfig hillsRenderConfig;

    private MultiMapDataStore mapDatabase;

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     *
     * @param cacheTileSourceName
     * @param minZoom
     * @param maxZoom
     * @param tileSizePixels
     * @param file
     * @param xmlRenderTheme      the theme to render tiles with
     * @param hillsRenderConfig   the hillshading setup to be used (can be null)
     * @param language            preferred language for map labels as defined in ISO 639-1 or ISO 639-2 (can be null)
     */
    protected MapsForgeTileSource(String cacheTileSourceName, int minZoom, int maxZoom, int tileSizePixels, File[] file, XmlRenderTheme xmlRenderTheme, MultiMapDataStore.DataPolicy dataPolicy, HillsRenderConfig hillsRenderConfig, final String language) {
        super(cacheTileSourceName, minZoom, maxZoom, tileSizePixels, ".png", "© OpenStreetMap contributors");

        mapDatabase = new MultiMapDataStore(dataPolicy);
        for (int i = 0; i < file.length; i++)
            mapDatabase.addMapDataStore(new MapFile(file[i], language), false, false);

        if (AndroidGraphicFactory.INSTANCE == null) {
            throw new RuntimeException("Must call MapsForgeTileSource.createInstance(context.getApplication()); once before MapsForgeTileSource.createFromFiles().");
        }

        // mapsforge0.6
        // renderer = new DatabaseRenderer(mapDatabase, AndroidGraphicFactory.INSTANCE, new InMemoryTileCache(2));
        // mapsforge0.6.1
        //InMemoryTileCache tileCache = new InMemoryTileCache(2);
        //renderer = new DatabaseRenderer(mapDatabase, AndroidGraphicFactory.INSTANCE, tileCache,
        //        new TileBasedLabelStore(tileCache.getCapacityFirstLevel()), true, true);
        // mapsforge0.8.0
        //InMemoryTileCache tileCache = new InMemoryTileCache(2);
        //renderer = new DatabaseRenderer(mapDatabase, AndroidGraphicFactory.INSTANCE, tileCache,
        //        new TileBasedLabelStore(tileCache.getCapacityFirstLevel()), true, true, hillsRenderConfig);
        // mapsforge0.11.0
        renderer = new DirectRenderer(mapDatabase, AndroidGraphicFactory.INSTANCE, true, hillsRenderConfig);

        minZoom = MIN_ZOOM;
        maxZoom = renderer.getZoomLevelMax();

        Log.d(IMapView.LOGTAG, "min=" + minZoom + " max=" + maxZoom + " tilesize=" + tileSizePixels);

        if (xmlRenderTheme == null)
            xmlRenderTheme = InternalRenderTheme.OSMARENDER;
        //we the passed in theme is different that the existing one, or the theme is currently null, create it
        if (xmlRenderTheme != mXmlRenderTheme || theme == null) {
            theme = new RenderThemeFuture(AndroidGraphicFactory.INSTANCE, xmlRenderTheme, model);
            //super important!! without the following line, all rendering activities will block until the theme is created.
            new Thread(theme).start();
        }
    }

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     *
     * @param cacheTileSourceName
     * @param minZoom
     * @param maxZoom
     * @param tileSizePixels
     * @param file
     * @param xmlRenderTheme      the theme to render tiles with
     * @param hillsRenderConfig   the hillshading setup to be used (can be null)
     */
    protected MapsForgeTileSource(String cacheTileSourceName, int minZoom, int maxZoom, int tileSizePixels, File[] file, XmlRenderTheme xmlRenderTheme, MultiMapDataStore.DataPolicy dataPolicy, HillsRenderConfig hillsRenderConfig) {
        this(cacheTileSourceName, minZoom, maxZoom, tileSizePixels, file, xmlRenderTheme, dataPolicy, hillsRenderConfig, null);
    }

    public BoundingBox getBounds() {
        return mapDatabase.boundingBox();
    }

    public org.osmdroid.util.BoundingBox getBoundsOsmdroid() {
        BoundingBox boundingBox = mapDatabase.boundingBox();
        final double latNorth = Math.min(MapView.getTileSystem().getMaxLatitude(), boundingBox.maxLatitude);
        final double latSouth = Math.max(MapView.getTileSystem().getMinLatitude(), boundingBox.minLatitude);
        return new org.osmdroid.util.BoundingBox(
                latNorth, boundingBox.maxLongitude,
                latSouth, boundingBox.minLongitude);
    }

    /**
     * Creates a new MapsForgeTileSource from file.
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @return the tile source
     */
    public static MapsForgeTileSource createFromFiles(File[] file) {
        //these settings are ignored and are set based on .map file info
        int minZoomLevel = MIN_ZOOM;
        int maxZoomLevel = MAX_ZOOM;
        int tileSizePixels = TILE_SIZE_PIXELS;

        return new MapsForgeTileSource(InternalRenderTheme.OSMARENDER.name(), minZoomLevel, maxZoomLevel, tileSizePixels, file, InternalRenderTheme.OSMARENDER, MultiMapDataStore.DataPolicy.RETURN_ALL, null, null);
    }

    /**
     * Creates a new MapsForgeTileSource from file[].
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @param theme     this can be null, in which case the default them will be used
     * @param themeName when using a custom theme, this sets up the osmdroid caching correctly
     * @return
     */
    public static MapsForgeTileSource createFromFiles(File[] file, XmlRenderTheme theme, String themeName) {
        //these settings are ignored and are set based on .map file info
        int minZoomLevel = MIN_ZOOM;
        int maxZoomLevel = MAX_ZOOM;
        int tileSizePixels = TILE_SIZE_PIXELS;

        return new MapsForgeTileSource(themeName, minZoomLevel, maxZoomLevel, tileSizePixels, file, theme, MultiMapDataStore.DataPolicy.RETURN_ALL, null, null);
    }

    /**
     * Creates a new MapsForgeTileSource from file[].
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @param theme     this can be null, in which case the default them will be used
     * @param themeName when using a custom theme, this sets up the osmdroid caching correctly
     * @param language  preferred language for map labels as defined in ISO 639-1 or ISO 639-2 (can be null)
     * @return
     */
    public static MapsForgeTileSource createFromFiles(File[] file, XmlRenderTheme theme, String themeName, final String language) {
        //these settings are ignored and are set based on .map file info
        int minZoomLevel = MIN_ZOOM;
        int maxZoomLevel = MAX_ZOOM;
        int tileSizePixels = TILE_SIZE_PIXELS;

        return new MapsForgeTileSource(themeName, minZoomLevel, maxZoomLevel, tileSizePixels, file, theme, MultiMapDataStore.DataPolicy.RETURN_ALL, null, language);
    }

    /**
     * Creates a new MapsForgeTileSource from file[].
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @param theme             this can be null, in which case the default them will be used
     * @param themeName         when using a custom theme, this sets up the osmdroid caching correctly
     * @param dataPolicy        use this to override the default, which is "RETURN_ALL"
     * @param hillsRenderConfig the hillshading setup to be used (can be null)
     * @return
     */
    public static MapsForgeTileSource createFromFiles(File[] file, XmlRenderTheme theme, String themeName, MultiMapDataStore.DataPolicy dataPolicy, HillsRenderConfig hillsRenderConfig) {
        //these settings are ignored and are set based on .map file info
        int minZoomLevel = MIN_ZOOM;
        int maxZoomLevel = MAX_ZOOM;
        int tileSizePixels = TILE_SIZE_PIXELS;

        return new MapsForgeTileSource(themeName, minZoomLevel, maxZoomLevel, tileSizePixels, file, theme, dataPolicy, hillsRenderConfig, null);
    }

    /**
     * Creates a new MapsForgeTileSource from file[].
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @param theme             this can be null, in which case the default them will be used
     * @param themeName         when using a custom theme, this sets up the osmdroid caching correctly
     * @param dataPolicy        use this to override the default, which is "RETURN_ALL"
     * @param hillsRenderConfig the hillshading setup to be used (can be null)
     * @param language          preferred language for map labels as defined in ISO 639-1 or ISO 639-2 (can be null)
     * @return
     */
    public static MapsForgeTileSource createFromFiles(File[] file, XmlRenderTheme theme, String themeName, MultiMapDataStore.DataPolicy dataPolicy, HillsRenderConfig hillsRenderConfig, final String language) {
        //these settings are ignored and are set based on .map file info
        int minZoomLevel = MIN_ZOOM;
        int maxZoomLevel = MAX_ZOOM;
        int tileSizePixels = TILE_SIZE_PIXELS;

        return new MapsForgeTileSource(themeName, minZoomLevel, maxZoomLevel, tileSizePixels, file, theme, dataPolicy, hillsRenderConfig, language);
    }


    //The synchronized here is VERY important.  If missing, the mapDatabase read gets corrupted by multiple threads reading the file at once.
    public synchronized Drawable renderTile(final long pMapTileIndex) {

        Tile tile = new Tile(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), (byte) MapTileIndex.getZoom(pMapTileIndex), 256);
        model.setFixedTileSize(256);

        //You could try something like this to load a custom theme
        //try{
        //	jobTheme = new ExternalRenderTheme(themeFile);
        //}
        //catch(Exception e){
        //	jobTheme = InternalRenderTheme.OSMARENDER;
        //}


        if (mapDatabase == null)
            return null;
        try {
            //Draw the tile
            RendererJob mapGeneratorJob = new RendererJob(tile, mapDatabase, theme, model, scale, false, false);
            AndroidTileBitmap bmp = (AndroidTileBitmap) renderer.executeJob(mapGeneratorJob);
            if (bmp != null)
                return new BitmapDrawable(AndroidGraphicFactory.getBitmap(bmp));
        } catch (Exception ex) {
            Log.d(IMapView.LOGTAG, "###################### Mapsforge tile generation failed", ex);
        }
        return null;
    }

    public static void createInstance(Application app) {
        AndroidGraphicFactory.createInstance(app);
    }


    public void dispose() {
        theme.decrementRefCount();
        theme = null;
        renderer = null;
        if (mapDatabase != null)
            mapDatabase.close();
        mapDatabase = null;
    }

    /**
     * @since 6.0.3
     */
    public void addTileRefresher(DirectRenderer.TileRefresher pDirectTileRefresher) {
        if (pDirectTileRefresher != null) {
            renderer.addTileRefresher(pDirectTileRefresher);
        }
    }
}

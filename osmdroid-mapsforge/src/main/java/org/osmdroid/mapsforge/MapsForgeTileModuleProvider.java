package org.osmdroid.mapsforge;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Adapted from code from here: https://github.com/MKergall/osmbonuspack, which is LGPL
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 *
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileModuleProvider extends MapTileFileStorageProviderBase {

    protected MapsForgeTileSource tileSource;
    protected IFilesystemCache tilewriter;

    /**
     * Constructor
     *
     * @param receiverRegistrar
     * @param tileSource
     */
    public MapsForgeTileModuleProvider(IRegisterReceiver receiverRegistrar, MapsForgeTileSource tileSource, IFilesystemCache tilewriter) {

        super(receiverRegistrar,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());

        this.tileSource = tileSource;
        this.tilewriter = tilewriter;

    }

    @Override
    protected String getName() {
        return "MapsforgeTiles Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "mapsforgetilesprovider";
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
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        //prevent re-assignment of tile source
        if (tileSource instanceof MapsForgeTileSource) {
            this.tileSource = (MapsForgeTileSource) tileSource;
        }
    }

    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final long pMapTileIndex) {
            //TODO find a more efficient want to do this, seems overly complicated
            String dbgPrefix = null;
            if (Configuration.getInstance().isDebugTileProviders()) {
                dbgPrefix = "MapsForgeTileModuleProvider.TileLoader.loadTile(" + MapTileIndex.toString(pMapTileIndex) + "): ";
                Log.d(IMapView.LOGTAG, dbgPrefix + "tileSource.renderTile");
            }
            Drawable image = tileSource.renderTile(pMapTileIndex);
            if (image != null && image instanceof BitmapDrawable) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ((BitmapDrawable) image).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                try {
                    stream.close();
                } catch (IOException e) {
                    //NO OP
                }

                if (Configuration.getInstance().isDebugTileProviders()) {
                    Log.d(IMapView.LOGTAG, dbgPrefix +
                            "save tile " + bitmapdata.length +
                            " bytes to " + tileSource.getTileRelativeFilenameString(pMapTileIndex));
                }

                ByteArrayInputStream bais = null;
                try {
                    bais = new ByteArrayInputStream(bitmapdata);
                    tilewriter.saveFile(tileSource, pMapTileIndex, bais, null);
                } catch (Exception ex) {
                    Log.w(IMapView.LOGTAG, "forge error storing tile cache", ex);
                } finally {
                    if (bais != null)
                        try {
                            bais.close();
                        } catch (IOException e) {
                            //NO OP
                        }
                }

            }
            return image;
        }
    }

}
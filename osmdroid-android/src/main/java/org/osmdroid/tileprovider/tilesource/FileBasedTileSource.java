package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.tileprovider.modules.OfflineTileProvider;

/**
 * this is an extremely simple tile source that should only be used for offline sources. assumes that the file name matches the source name
 *
 * @author alex
 * @see OfflineTileProvider
 */
public class FileBasedTileSource extends XYTileSource {

    public FileBasedTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
    }

    public static ITileSource getSource(String name) {
        if (name.contains(".")) {
            name = name.substring(0, name.indexOf("."));
        }
        return new FileBasedTileSource(name,
                0, 18, 256, ".png", new String[]{
                "http://localhost"});
    }
}

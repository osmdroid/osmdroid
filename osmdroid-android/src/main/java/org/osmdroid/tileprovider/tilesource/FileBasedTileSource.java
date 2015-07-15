/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;

/**
 * this is an extremely simple tile source that should only be used for offline sources.
 * @see OfflineTileProvider
 * @author alex
 */
public class FileBasedTileSource extends XYTileSource {

	public FileBasedTileSource(String aName, ResourceProxy.string aResourceId, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
	}

	public static ITileSource getSource(String name) {
		if (name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		return new FileBasedTileSource(name,
			ResourceProxy.string.mapbox, 0, 18, 256, ".png", new String[]{
				"http://localhost"});
	}
}

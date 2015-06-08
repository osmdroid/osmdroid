// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.views.util;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.MapView;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public class MapTileProviderFactory implements MapViewConstants {

	private static final Logger logger = LoggerFactory.getLogger(MapTileProviderFactory.class);

	/**
	 * Get a tile provider. If a tile provider service exists then it will use the service,
	 * otherwise it'll use a direct tile provider that doesn't use a service. This can be used as
	 * the tile provider parameter in the {@link MapView} constructor.
	 */
	public static MapTileProviderBase getInstance(final Context aContext) {
		logger.info("Using direct tile provider");
		return new MapTileProviderBasic(aContext.getApplicationContext());
	}

	/**
	 * This is a utility class with only static members.
	 */
	private MapTileProviderFactory() {
	}
}

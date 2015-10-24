// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.views.util;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.MapView;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.content.Context;
import android.util.Log;
import org.osmdroid.api.IMapView;

/**
 *
 * @author Nicolas Gramlich
 *
 */
@Deprecated
public class MapTileProviderFactory implements MapViewConstants {

	/**
	 * Get a tile provider. If a tile provider service exists then it will use the service,
	 * otherwise it'll use a direct tile provider that doesn't use a service. This can be used as
	 * the tile provider parameter in the {@link MapView} constructor.
	 */
	public static MapTileProviderBase getInstance(final Context aContext) {
          Log.i(IMapView.LOGTAG,"Using direct tile provider");
		return new MapTileProviderBasic(aContext.getApplicationContext());
	}

	/**
	 * This is a utility class with only static members.
	 */
	private MapTileProviderFactory() {
	}
}

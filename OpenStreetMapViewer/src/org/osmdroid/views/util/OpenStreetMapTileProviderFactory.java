// Created by plusminus on 21:46:22 - 25.09.2008
package org.osmdroid.views.util;

import org.osmdroid.tileprovider.OpenStreetMapTileProviderBase;
import org.osmdroid.tileprovider.OpenStreetMapTileProviderDirect;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.OpenStreetMapView;
import org.osmdroid.views.util.constants.OpenStreetMapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Handler;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class OpenStreetMapTileProviderFactory implements OpenStreetMapViewConstants {

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStreetMapTileProviderFactory.class);

	/**
	 * Get a tile provider. If a tile provider service exists then it will use the service,
	 * otherwise it'll use a direct tile provider that doesn't use a service. This can be used as
	 * the tile provider parameter in the {@link OpenStreetMapView} constructor.
	 * 
	 * @param pContext
	 * @param pDownloadFinishedListener
	 * @return
	 */
	public static OpenStreetMapTileProviderBase getInstance(final Context aContext,
			final Handler aDownloadFinishedListener, final String aCloudmadeKey) {
		logger.info("Using direct tile provider");
		CloudmadeUtil.retrieveCloudmadeKey(aContext);
		return new OpenStreetMapTileProviderDirect(aContext.getApplicationContext());
	}

	/**
	 * This is a utility class with only static members.
	 */
	private OpenStreetMapTileProviderFactory() {
	}
}

// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.services.IOpenStreetMapTileProviderService;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class OpenStreetMapTileProviderFactory implements OpenStreetMapViewConstants {
	
	public static final String DEBUGTAG = "OpenStreetMapTileProviderFactory";

	/**
	 * Get a tile provider.
	 * If a tile provider service exists then it will use the service,
	 * otherwise it'll use a direct tile provider that doesn't use a service
	 * @param pContext
	 * @param pDownloadFinishedListener
	 * @return
	 */
	public static OpenStreetMapTileProvider getInstance(final Context pContext,
			final Handler pDownloadFinishedListener) {
		final Intent intent = new Intent(IOpenStreetMapTileProviderService.class.getName());
		final ResolveInfo ri = pContext.getPackageManager().resolveService(intent, 0);
		if (ri == null) {
			Log.i(DEBUGTAG, "Service not found - using direct tile provider");
			return new OpenStreetMapTileProviderDirect(pDownloadFinishedListener);
		} else {
			Log.i(DEBUGTAG, "Using tile provider service");
			return new OpenStreetMapTileProviderService(pContext, pDownloadFinishedListener);
			// XXX Perhaps we should pass the Intent or the class name (action) into
			//     this constructor since we do the same again in there.
			//     That will also give the option of specifying something else.
		}
	}
	
	/**
	 * This is a utility class with only static members.
	 */
	private OpenStreetMapTileProviderFactory() {
	}
}

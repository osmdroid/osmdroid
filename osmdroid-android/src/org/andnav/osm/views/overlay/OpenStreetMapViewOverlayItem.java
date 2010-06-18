// Created by plusminus on 00:02:58 - 03.10.2008
package org.andnav.osm.views.overlay;

import org.andnav.osm.util.GeoPoint;

/**
 * Immutable class describing a GeoPoint with a Title and a Description.
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapViewOverlayItem {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	public final String mTitle;
	public final String mDescription;
	public final GeoPoint mGeoPoint;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	/**
	 * @param aTitle this should be <b>singleLine</b> (no <code>'\n'</code> )
	 * @param aDescription a <b>multiLine</b> description ( <code>'\n'</code> possible)
	 * @param aGeoPoint
	 */
	public OpenStreetMapViewOverlayItem(final String aTitle, final String aDescription, final GeoPoint aGeoPoint) {
		this.mTitle = aTitle;
		this.mDescription = aDescription;
		this.mGeoPoint = aGeoPoint;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

// Created by plusminus on 08:19:56 - 26.09.2008
package org.andnav.osm.views.util;


/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class OpenStreetMapTileNameFormatter {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	
	/**
	 * Formats a URL to a String that it can be saved to a file, without problems of special chars.
	 * 
	 * <PRE><b>Example:</b>
	 * 
	 * <code>http://a.tile.openstreetmap.org/0/0/0.png</code>
	 * would become 
	 * <code>a.tile.openstreetmap.org_0_0_0.png</code>
	 * </PRE>
	 * @return saveable formatted URL as a String
	 */
	public static String format(final String aTileURLString){
		return aTileURLString.substring(7).replace("/", "_");
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

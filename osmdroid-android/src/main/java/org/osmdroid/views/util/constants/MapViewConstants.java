// Created by plusminus on 18:00:24 - 25.09.2008
package org.osmdroid.views.util.constants;

import org.osmdroid.config.Configuration;

/**
 * 
 * This class contains constants used by the map view.
 * 
 * @author Nicolas Gramlich
 *
 * @deprecated  will be removed in osmdroid v6

 */
public interface MapViewConstants {

	/**
	 * will be removed in osmdroid v6
	 */
	@Deprecated
	public static boolean DEBUGMODE = Configuration.getInstance().isDebugMapView();

	// ===========================================================
	// Final Fields
	// ===========================================================

	/**
	 * will be removed in osmdroid v6
	 */
	public static final int NOT_SET = Integer.MIN_VALUE;

	/**
	 * will be removed in osmdroid v6
	 */
	public static final int ANIMATION_SMOOTHNESS_LOW = 4;
	/**
	 * will be removed in osmdroid v6
	 */
	public static final int ANIMATION_SMOOTHNESS_DEFAULT = 10;
	/**
	 * will be removed in osmdroid v6
	 */
	public static final int ANIMATION_SMOOTHNESS_HIGH = 20;

	/**
	 * will be removed in osmdroid v6
	 */
	@Deprecated
	public static final int ANIMATION_DURATION_SHORT = Configuration.getInstance().getAnimationSpeedShort();
	/**
	 * will be removed in osmdroid v6
	 */
	@Deprecated
	public static final int ANIMATION_DURATION_DEFAULT = Configuration.getInstance().getAnimationSpeedDefault();
	/**
	 * will be removed in osmdroid v6
	 */
	@Deprecated
	public static final int ANIMATION_DURATION_LONG = 2000;

	/** Minimum Zoom Level
	 * will be removed in osmdroid v6*/
	public static final int MINIMUM_ZOOMLEVEL = 0;
     public static final int MAXIMUM_ZOOMLEVEL = 23;
}

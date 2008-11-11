// Created by plusminus on 18:58:15 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.views.util.constants.MathConstants;


/**
 * http://wiki.openstreetmap.org/index.php/Mercator
 * @author Nicolas Gramlich
 * This class provides a way to convert from latitude and longitude to a simple Mercator projection.
 */
public class Mercator implements MathConstants {
	// ===========================================================
	// Constants
	// ===========================================================

//	private static final double R_MAJOR = 6378137.0;
//	private static final double R_MINOR = 6356752.3142;
//	private static final double RATIO = R_MINOR / R_MAJOR;
//	
//		
//	private static final double ECCENT = Math.sqrt(1.0 - (RATIO * RATIO));
//	private static final double COM = 0.5 * ECCENT;




	// ===========================================================
	// Static Methods
	// ===========================================================
	
	/**
	 * Converts a Mercator-projected y-coordinate (projected latitude) to the real Latitude. This is only a approximation. 
	 */
	public static double y2lat(final double a) { return 180/Math.PI * (2 * Math.atan(Math.exp(a*Math.PI/180)) - Math.PI/2); }
	
	/**
	 * Converts a real Latitude to the Mercator-projected y-coordinate (projected latitude) . This is only a approximation. 
	 */
	public static double lat2y(final double a) { return 180/Math.PI * Math.log(Math.tan(Math.PI/4+a*(Math.PI/180)/2)); }
	
	/**
	 * Converts a Mercator-projected x-coordinate (projected longitude) to the real longitude. This is only a approximation. 
	 */
	public static double x2lon(final double x){ return x * RAD2DEG; }
	
	/**
	 * Converts a real longitude to the Mercator-projected x-coordinate (projected longitude). This is only a approximation. 
	 */
	public static double lon2x(final double lon) { return DEG2RAD * lon; }
	
//	public static double[] fromMercatorProjection(final int x, final int y){
//		return new double[] {projectedToRealLat(y / 1E6), projectedToRealLon(x / 1E6)};
//	}
//	
//	public static double[] fromMercatorProjection(final double x, final double y){
//		return new double[] {projectedToRealLat(y), projectedToRealLon(x)};
//	}
//
//	private static double projectedToRealLon(final double x){
//		return x * RAD2DEG / R_MAJOR;
//	}
//
//	private static double projectedToRealLat(final double y){
//		final double ts = Math.exp(-y / R_MAJOR);
//		double phi = PI_2 - 2 * Math.atan(ts);
//		double dphi = 1.0;
//		int i = 0;
//		while((Math.abs(dphi) > 0.000000001) && (i < 15)) {
//			final double con = ECCENT * Math.sin(phi);
//			dphi = PI_2 - 2 * Math.atan(ts * Math.pow((1.0 - con) / (1.0 + con), COM)) - phi;
//			phi += dphi;
//			i++;
//		}
//		return RAD2DEG * phi;
//	}
//
//	public static double[] toMercatorProjection(final double aLatitude, final double aLongitude) {
//		return new double[] {realToProjectedLat(aLatitude), realToProjectedLon(aLongitude)};
//	}
//	
//	public static double[] toMercatorProjection(final int aLatitudeE6, final int aLongitudeE6) {
//		return new double[] {realToProjectedLat(aLatitudeE6 / 1E6), realToProjectedLon(aLongitudeE6 / 1E6)};
//	}
//
//	private static double realToProjectedLon(final double lon) {
//		return R_MAJOR * DEG2RAD * lon;
//	}
//
//	private static double realToProjectedLat(double lat) {
//		if (lat > 89.5) {
//			lat = 89.5;
//		}
//		if (lat < -89.5) {
//			lat = -89.5;
//		}
//		
//		final double phi = DEG2RAD * lat;
//		final double sinphi = Math.sin(phi);
//		double con = ECCENT * sinphi;
//		con = Math.pow(((1.0 - con) / (1.0 + con)), COM);
//		final double ts = Math.tan(0.5 * (PI_2 - phi)) / con;
//		final double y = 0 - R_MAJOR * Math.log(ts);
//		return y;
//	}
}

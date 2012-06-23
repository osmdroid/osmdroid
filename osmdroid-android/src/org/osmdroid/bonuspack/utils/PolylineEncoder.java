package org.osmdroid.bonuspack.utils;

import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;

/**
 * Methods to encode and decode a polyline with Google polyline encoding/decoding scheme. 
 * See https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineEncoder {
	
    private static StringBuffer encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return(encodeNumber(sgn_num));
    }

    private static StringBuffer encodeNumber(int num) {
        StringBuffer encodeString = new StringBuffer();
        while (num >= 0x20) {
                int nextValue = (0x20 | (num & 0x1f)) + 63;
                encodeString.append((char)(nextValue));
            num >>= 5;
        }
        num += 63;
        encodeString.append((char)(num));
        return encodeString;
    }
    
    /**
     * Encode a polyline with Google polyline encoding method
     * @param polyline the polyline
     * @param precision 1 for a 6 digits encoding, 10 for a 5 digits encoding. 
     * @return the encoded polyline, as a String
     */
    public static String encode(ArrayList<GeoPoint> polyline, int precision) {
		StringBuffer encodedPoints = new StringBuffer();
		int prev_lat = 0, prev_lng = 0;
		for (GeoPoint trackpoint:polyline) {
			int lat = trackpoint.getLatitudeE6() / precision;
			int lng = trackpoint.getLongitudeE6() / precision;
			encodedPoints.append(encodeSignedNumber(lat - prev_lat));
			encodedPoints.append(encodeSignedNumber(lng - prev_lng));			
			prev_lat = lat;
			prev_lng = lng;
		}
		return encodedPoints.toString();
	}
    
    /**
     * Decode a "Google-encoded" polyline
     * @param encodedString
     * @param precision 1 for a 6 digits encoding, 10 for a 5 digits encoding. 
     * @return the polyline. 
     */
    public static ArrayList<GeoPoint> decode(String encodedString, int precision) {
        ArrayList<GeoPoint> polyline = new ArrayList<GeoPoint>();
        int index = 0;
        int len = encodedString.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedString.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedString.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            GeoPoint p = new GeoPoint(lat*precision, lng*precision);
            polyline.add(p);
        }

        return polyline;
    }
}

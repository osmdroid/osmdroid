// Created by plusminus on 13:23:45 - 21.09.2008
package org.osmdroid.contributor.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.osmdroid.contributor.util.constants.OpenStreetMapContributorConstants;

/**
 * Class capable of formatting a List of Points to the GPX 1.1 format.
 * 
 * @author Nicolas Gramlich
 * 
 */
public class RecordedRouteGPXFormatter implements OpenStreetMapContributorConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final String XML_VERSION = "<?xml version=\"1.0\"?>";
	private static final String GPX_VERSION = "1.1";
	private static final String GPX_TAG = "<gpx version=\""
			+ GPX_VERSION
			+ "\" creator=\"%s\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">";
	private static final String GPX_TAG_CLOSE = "</gpx>";
	private static final String GPX_TAG_TIME = "<time>%s</time>";
	private static final String GPX_TAG_TRACK = "<trk>";
	private static final String GPX_TAG_TRACK_CLOSE = "</trk>";
	private static final String GPX_TAG_TRACK_NAME = "<name>%s</name>";
	private static final String GPX_TAG_TRACK_SEGMENT = "<trkseg>";
	private static final String GPX_TAG_TRACK_SEGMENT_CLOSE = "</trkseg>";
	public static final String GPX_TAG_TRACK_SEGMENT_POINT = "<trkpt lat=\"%f\" lon=\"%f\">";
	public static final String GPX_TAG_TRACK_SEGMENT_POINT_CLOSE = "</trkpt>";
	public static final String GPX_TAG_TRACK_SEGMENT_POINT_TIME = "<time>%s</time>";
	public static final String GPX_TAG_TRACK_SEGMENT_POINT_SAT = "<sat>%d</sat>";
	public static final String GPX_TAG_TRACK_SEGMENT_POINT_ELE = "<ele>%d</ele>";

	private static final SimpleDateFormat formatterCompleteDateTime = new SimpleDateFormat(
			"yyyyMMdd'_'HHmmss");

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
	 * Creates a String in the following XML format:
	 * 
	 * <PRE>
	 * &lt;?xml version=&quot;1.0&quot;?&gt;
	 * &lt;gpx version=&quot;1.1&quot; creator=&quot;AndNav - http://www.andnav.org - Android Navigation System&quot; xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns=&quot;http://www.topografix.com/GPX/1/1&quot; xsi:schemaLocation=&quot;http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd&quot;&gt;
	 * 	&lt;time&gt;2008-09-22T00:46:20Z&lt;/time&gt;
	 * 	&lt;trk&gt;
	 * 	&lt;name&gt;plusminus--yyyyMMdd_HHmmss-yyyyMMdd_HHmmss&lt;/name&gt;
	 * 		&lt;trkseg&gt;
	 * 			&lt;trkpt lat=&quot;49.472767&quot; lon=&quot;8.654174&quot;&gt;
	 * 				&lt;time&gt;2008-09-22T00:46:20Z&lt;/time&gt;
	 * 			&lt;/trkpt&gt;
	 * 			&lt;trkpt lat=&quot;49.472797&quot; lon=&quot;8.654102&quot;&gt;
	 * 				&lt;time&gt;2008-09-22T00:46:35Z&lt;/time&gt;
	 * 			&lt;/trkpt&gt;
	 * 			&lt;trkpt lat=&quot;49.472802&quot; lon=&quot;8.654185&quot;&gt;
	 * 				&lt;time&gt;2008-09-22T00:46:50Z&lt;/time&gt;
	 * 			&lt;/trkpt&gt;
	 * 		&lt;/trkseg&gt;
	 * 	&lt;/trk&gt;
	 * &lt;/gpx&gt;
	 * </PRE>
	 * 
	 */
	public static String create(final List<RecordedGeoPoint> someRecords)
			throws IllegalArgumentException {
		if (someRecords == null)
			throw new IllegalArgumentException("Records may not be null.");

		if (someRecords.size() == 0)
			throw new IllegalArgumentException("Records size == 0");

		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		sb.append(XML_VERSION);
		f.format(GPX_TAG, OSM_CREATOR_INFO);
		f.format(GPX_TAG_TIME, Util.convertTimestampToUTCString(System.currentTimeMillis()));
		sb.append(GPX_TAG_TRACK);
		f.format(
				GPX_TAG_TRACK_NAME,
				OSM_USERNAME
						+ "--"
						+ formatterCompleteDateTime.format(new Date(someRecords.get(0)
								.getTimeStamp()).getTime())
						+ "-"
						+ formatterCompleteDateTime.format(new Date(someRecords.get(
								someRecords.size() - 1).getTimeStamp()).getTime()));
		sb.append(GPX_TAG_TRACK_SEGMENT);

		for (final RecordedGeoPoint rgp : someRecords) {
			f.format(GPX_TAG_TRACK_SEGMENT_POINT, rgp.getLatitudeAsDouble(),
					rgp.getLongitudeAsDouble());
			f.format(GPX_TAG_TRACK_SEGMENT_POINT_TIME,
					Util.convertTimestampToUTCString(rgp.getTimeStamp()));
			if (rgp.mNumSatellites != NOT_SET)
				f.format(GPX_TAG_TRACK_SEGMENT_POINT_SAT, rgp.mNumSatellites);
			sb.append(GPX_TAG_TRACK_SEGMENT_POINT_CLOSE);
		}

		sb.append(GPX_TAG_TRACK_SEGMENT_CLOSE).append(GPX_TAG_TRACK_CLOSE).append(GPX_TAG_CLOSE);

		return sb.toString();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

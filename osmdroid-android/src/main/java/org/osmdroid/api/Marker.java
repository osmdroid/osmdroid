package org.osmdroid.api;

public class Marker {

	public double latitude;

	public double longitude;

	/**
	 * The title of the marker. If null then marker has no title.
	 */
	public String title;

	/**
	 * Snippet displayed below the title. If null then marker has no snippet.
	 */
	public String snippet;

	/**
	 * Resource id of marker. If zero then use default marker.
	 */
	public int icon;

	public Marker(final double aLatitude, final double aLongitude) {
		latitude = aLatitude;
		longitude = aLongitude;
	}

	public Marker(final double aLatitude, final double aLongitude, final String aTitle, final String aSnippet, final int aIcon) {
		this(aLatitude, aLongitude);
		title = aTitle;
		snippet = aSnippet;
		icon = aIcon;
	}
}

package org.osmdroid.api;

public class Marker {

	public final double latitude;

	public final double longitude;

	/**
	 * The title of the marker. If null then marker has no title.
	 */
	public String title;

	/**
	 * Convenience method for chaining.
	 */
	public Marker title(final String aTitle) {
		title = aTitle;
		return this;
	}

	/**
	 * Snippet displayed below the title. If null then marker has no snippet.
	 */
	public String snippet;

	/**
	 * Convenience method for chaining.
	 */
	public Marker snippet(final String aSnippet) {
		snippet = aSnippet;
		return this;
	}

	/**
	 * Resource id of marker. If zero then use default marker.
	 */
	public int icon;

	/**
	 * Convenience method for chaining.
	 */
	public Marker icon(final int aIcon) {
		icon = aIcon;
		return this;
	}

	public Marker(final double aLatitude, final double aLongitude) {
		latitude = aLatitude;
		longitude = aLongitude;
	}
}

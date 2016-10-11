package org.osmdroid.api;

import android.graphics.Bitmap;

/**
 * this is only used by the Google Wrapper/3rd party library
 */
public class Marker {

	public enum Anchor {
		NONE,
		CENTER, BOTTOM_CENTER // these are the only two supported by Google Maps v1
	}

	public final double latitude;

	public final double longitude;

	/**
	 * The title of the marker. If null then marker has no title.
	 */
	public String title;

	/**
	 * The title of the marker. If null then marker has no title.
	 * This method returns the marker for convenient method chaining.
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
	 * Snippet displayed below the title. If null then marker has no snippet.
	 * This method returns the marker for convenient method chaining.
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
	 * Resource id of marker. If zero then use default marker.
	 * This method returns the marker for convenient method chaining.
	 */
	public Marker icon(final int aIcon) {
		icon = aIcon;
		return this;
	}

	/**
	 * Bitmap of marker. If null then use {@link #icon}.
	 */
	public Bitmap bitmap;

	/**
	 * Bitmap of marker. If null then use {@link #icon}.
	 * This method returns the marker for convenient method chaining.
	 */
	public Marker bitmap(final Bitmap aBitmap) {
		bitmap = aBitmap;
		return this;
	}

	/*
	 * Anchor of marker. Default is {@link Anchor#BOTTOM_CENTER}.
	 */
	public Anchor anchor;

	/**
	 * Anchor of marker. Default is {@link Anchor#BOTTOM_CENTER}.
	 * This method returns the marker for convenient method chaining.
	 */
	public Marker anchor(final Anchor aAnchor) {
		anchor = aAnchor;
		return this;
	}

	public Marker(final double aLatitude, final double aLongitude) {
		latitude = aLatitude;
		longitude = aLongitude;
	}
}

// Created by plusminus on 12:29:23 - 21.09.2008
package org.osmdroid.contributor.util;

import org.osmdroid.contributor.util.constants.OpenStreetMapContributorConstants;
import org.osmdroid.util.GeoPoint;

/**
 * Extends the {@link GeoPoint} with a timeStamp.
 *
 * @author Nicolas Gramlich
 */
public class RecordedGeoPoint extends GeoPoint implements OpenStreetMapContributorConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final long serialVersionUID = 7304941424576720318L;

	// ===========================================================
	// Fields
	// ===========================================================

	protected final long mTimeStamp;
	protected final int mNumSatellites;

	// ===========================================================
	// Constructors
	// ===========================================================

	public RecordedGeoPoint(final double latitude, final double longitude) {
		this(latitude, longitude, System.currentTimeMillis(), NOT_SET);
	}

	public RecordedGeoPoint(final double latitude, final double longitude, final long aTimeStamp,
			final int aNumSatellites) {
		super(latitude, longitude);
		this.mTimeStamp = aTimeStamp;
		this.mNumSatellites = aNumSatellites;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public long getTimeStamp() {
		return this.mTimeStamp;
	}

	public double getLatitudeAsDouble() {
		return this.getLatitude();
	}

	public double getLongitudeAsDouble() {
		return this.getLongitude();
	}

	public int getNumSatellites() {
		return this.mNumSatellites;
	}

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

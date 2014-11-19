// Created by plusminus on 12:28:16 - 21.09.2008
package org.osmdroid.contributor;

import java.util.ArrayList;

import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.util.GeoPoint;

import android.location.Location;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class RouteRecorder {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final ArrayList<RecordedGeoPoint> mRecords = new ArrayList<RecordedGeoPoint>();

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public ArrayList<RecordedGeoPoint> getRecordedGeoPoints() {
		return this.mRecords;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void add(final Location aLocation, final int aNumSatellites) {
		this.mRecords
				.add(new RecordedGeoPoint(aLocation.getLatitude(), aLocation.getLongitude(),
                        System.currentTimeMillis(), aNumSatellites));
	}

	public void add(final GeoPoint aGeoPoint, final int aNumSatellites) {
		this.mRecords.add(new RecordedGeoPoint(aGeoPoint.getLatitude(), aGeoPoint
				.getLongitude(), System.currentTimeMillis(), aNumSatellites));
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

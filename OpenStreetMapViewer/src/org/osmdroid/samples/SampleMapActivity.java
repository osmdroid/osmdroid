// Created by plusminus on 00:14:42 - 02.10.2008
package org.osmdroid.samples;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.contributor.OSMUploader;
import org.osmdroid.contributor.RouteRecorder;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Baseclass for Activities who want to contribute to the OpenStreetMap Project.
 *
 * @author Nicolas Gramlich
 *
 */
public abstract class SampleMapActivity extends Activity implements OpenStreetMapConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	protected static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;

	// ===========================================================
	// Fields
	// ===========================================================

	protected SampleLocationListener mLocationListener;

	protected RouteRecorder mRouteRecorder = new RouteRecorder();

	protected boolean mDoGPSRecordingAndContributing;

	protected LocationManager mLocationManager;

	public int mNumSatellites = NOT_SET;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * Calls
	 * <code>onCreate(final Bundle savedInstanceState, final boolean pDoGPSRecordingAndContributing)</code>
	 * with <code>pDoGPSRecordingAndContributing == true</code>.<br/>
	 * That means it automatically contributes to the OpenStreetMap Project in the background.
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		onCreate(savedInstanceState, true);
	}

	/**
	 * Called when the activity is first created. Registers LocationListener.
	 *
	 * @param savedInstanceState
	 * @param pDoGPSRecordingAndContributing
	 *            If <code>true</code>, it automatically contributes to the OpenStreetMap Project in
	 *            the background.
	 */
	public void onCreate(final Bundle savedInstanceState,
			final boolean pDoGPSRecordingAndContributing) {
		super.onCreate(savedInstanceState);

		if (pDoGPSRecordingAndContributing)
			this.enableDoGPSRecordingAndContributing();
		else
			this.disableDoGPSRecordingAndContributing(false);
	}

	private LocationManager getLocationManager() {
		if (this.mLocationManager == null)
			this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return this.mLocationManager;
	}

	private void initLocation() {
		this.mLocationListener = new SampleLocationListener();
		getLocationManager()
				.requestLocationUpdates(PROVIDER_NAME, 2000, 20, this.mLocationListener);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	public abstract void onLocationLost();

	public abstract void onLocationChanged(final Location pLoc);

	@Override
	protected void onResume() {
		// register location listener
		initLocation();

		super.onResume();
	}

	@Override
	protected void onPause() {
		getLocationManager().removeUpdates(mLocationListener);

		if (this.mDoGPSRecordingAndContributing) {
			OSMUploader.uploadAsync(this.mRouteRecorder.getRecordedGeoPoints());
		}

		super.onPause();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void enableDoGPSRecordingAndContributing() {
		/* If already true, return. */
		if (this.mDoGPSRecordingAndContributing)
			return;

		this.mRouteRecorder = new RouteRecorder();

		this.mDoGPSRecordingAndContributing = true;
	}

	public void disableDoGPSRecordingAndContributing(final boolean pContributdeCurrentRoute) {
		/* If already false, return. */
		if (!this.mDoGPSRecordingAndContributing)
			return;

		if (pContributdeCurrentRoute) {
			OSMUploader.uploadAsync(this.mRouteRecorder.getRecordedGeoPoints());
		}

		this.mRouteRecorder = null;

		this.mDoGPSRecordingAndContributing = false;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * Logs all Location-changes to <code>mRouteRecorder</code>.
	 *
	 * @author plusminus
	 */
	private class SampleLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(final Location loc) {
			if (loc != null) {
				if (SampleMapActivity.this.mDoGPSRecordingAndContributing)
					SampleMapActivity.this.mRouteRecorder.add(loc,
							SampleMapActivity.this.mNumSatellites);

				SampleMapActivity.this.onLocationChanged(loc);
			} else {
				SampleMapActivity.this.onLocationLost();
			}
		}

		@Override
		public void onStatusChanged(final String a, final int i, final Bundle b) {
			// TODO Check on an actual device
			SampleMapActivity.this.mNumSatellites = b.getInt("satellites", NOT_SET);
		}

		@Override
		public void onProviderEnabled(final String a) { /* ignore */
		}

		@Override
		public void onProviderDisabled(final String a) { /* ignore */
		}
	}
}

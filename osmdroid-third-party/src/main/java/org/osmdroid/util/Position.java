package org.osmdroid.util;

import org.osmdroid.api.IPosition;

/**
 * this is only used in the Google Wrapper sample and will move to osmdroid-3rd party
 */
public class Position implements IPosition {
	private final double mLatitude;
	private final double mLongitude;
	private boolean mHasBearing;
	private float mBearing;
	private boolean mHasZoomLevel;
	private float mZoomLevel;

	public Position(final double aLatitude, final double aLongitude) {
		mLatitude = aLatitude;
		mLongitude = aLongitude;
	}

	@Override
	public double getLatitude() {
		return mLatitude;
	}

	@Override
	public double getLongitude() {
		return mLongitude;
	}

	@Override
	public boolean hasBearing() {
		return mHasBearing;
	}

	@Override
	public float getBearing() {
		return mBearing;
	}

	public void setBearing(final float aBearing) {
		mHasBearing = true;
		mBearing = aBearing;
	}

	@Override
	public boolean hasZoomLevel() {
		return mHasZoomLevel;
	}

	@Override
	public float getZoomLevel() {
		return mZoomLevel;
	}

	public void setZoomLevel(final float aZoomLevel) {
		mHasZoomLevel = true;
		mZoomLevel = aZoomLevel;
	}
}

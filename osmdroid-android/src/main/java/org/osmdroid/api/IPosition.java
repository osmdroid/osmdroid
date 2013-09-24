package org.osmdroid.api;

public interface IPosition {

	double getLatitude();

	double getLongitude();

	boolean hasBearing();

	float getBearing();

	boolean hasZoomLevel();

	float getZoomLevel();
}

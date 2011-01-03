package org.osmdroid.api;

import org.osmdroid.util.GeoPoint;

import android.graphics.Point;

public interface IProjection {

	Point toPixels(GeoPoint in, android.graphics.Point out);
	GeoPoint fromPixels(int x, int y);
	float metersToEquatorPixels(float meters);

}

package org.osmdroid.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.graphics.Color;

public class Polyline {

	private static final Random random = new Random();

	public Polyline() {
		id = random.nextLong();
		points = new ArrayList<IGeoPoint>();
	}

	/**
	 * The id of the polyline.
	 * This is needed for adding points with {@link org.osmdroid.api.IMap#addPointToPolyline(long, IGeoPoint)}.
	 */
	public final long id;

	/**
	 * The color of the polyline. Defaults to black.
	 */
	public int color = Color.BLACK;

	/**
	 * The color of the polyline. Defaults to black.
	 * This method returns the polyline for convenient method chaining.
	 */
	public Polyline color(final int aColor) {
		color = aColor;
		return this;
	}

	/**
	 * The width of the polyline. Defaults to 2.
	 */
	public float width = 2.0f;

	/**
	 * The width of the polyline. Defaults to 2.
	 * This method returns the polyline for convenient method chaining.
	 */
	public Polyline width(final float aWidth) {
		width = aWidth;
		return this;
	}

	/**
	 * The points of the polyline.
	 */
	public List<IGeoPoint> points;

	/**
	 * The points of the polyline.
	 * This method returns the polyline for convenient method chaining.
	 */
	public Polyline points(final List<IGeoPoint> aPoints) {
		points = aPoints;
		return this;
	}

	/**
	 * The points of the polyline.
	 * This method returns the polyline for convenient method chaining.
	 */
	public Polyline points(final IGeoPoint... aPoints) {
		return points(Arrays.asList(aPoints));
	}
}

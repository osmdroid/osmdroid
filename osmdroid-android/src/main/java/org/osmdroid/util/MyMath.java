// Created by plusminus on 20:36:01 - 26.09.2008
package org.osmdroid.util;

import org.osmdroid.views.util.constants.MathConstants;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class MyMath implements MathConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * This is a utility class with only static members.
	 */
	private MyMath() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static double gudermannInverse(final double aLatitude) {
		return Math.log(Math.tan(PI_4 + (DEG2RAD * aLatitude / 2)));
	}

	public static double gudermann(final double y) {
		return RAD2DEG * Math.atan(Math.sinh(y));
	}

	public static int mod(int number, final int modulus) {
		if (number > 0)
			return number % modulus;

		while (number < 0)
			number += modulus;

		return number;
	}

	/**
	 * Casting a _negative_ double into a long has a counter-intuitive result.
	 * E.g. (long)(-0.4) = 0, though -1 would be expected.
	 * Math.floor would be the answer, but I assume we could go faster than (long)Math.floor
	 * @since 6.0.0
	 */
	public static long floorToLong(final double pValue) {
		final long result = (long) pValue;
		if (result <= pValue) {
			return result;
		}
		return result - 1;
	}

	/**
	 * @since 6.0.0
	 */
	public static int floorToInt(final double pValue) {
		final int result = (int) pValue;
		if (result <= pValue) {
			return result;
		}
		return result - 1;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

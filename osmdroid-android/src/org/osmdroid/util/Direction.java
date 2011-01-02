// Created by plusminus on 00:07:38 - 22.01.2009
package org.osmdroid.util;

public enum Direction {
	NORTH("North"), // TODO i18n
	EAST("East"), SOUTH("South"), WEST("West"), NORTHEAST("North East"), SOUTHEAST("South East"), SOUTHWEST(
			"South West"), NORTHWEST("North West"), CENTER("Center");

	public final String NAME; // <-- int NAMERESID

	private Direction(final String pName) {
		this.NAME = pName;
	}

	public static Direction[] getDiagonalDirections() {
		return new Direction[] { NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST };
	}

	public static Direction[] getStraightDirections() {
		return new Direction[] { NORTH, EAST, SOUTH, WEST };
	}
}

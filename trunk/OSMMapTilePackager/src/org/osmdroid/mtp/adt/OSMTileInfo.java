// Created by plusminus on 00:37:01 - 19.12.2008
package org.osmdroid.mtp.adt;

public class OSMTileInfo {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int POSITION_IN_PARENT_LEFT = 1;
	private static final int POSITION_IN_PARENT_RIGHT = 2;
	private static final int POSITION_IN_PARENT_BOTTOM = 4;
	private static final int POSITION_IN_PARENT_TOP = 8;

	public static final int POSITION_IN_PARENT_TOPLEFT = POSITION_IN_PARENT_LEFT | POSITION_IN_PARENT_TOP;
	public static final int POSITION_IN_PARENT_TOPRIGHT = POSITION_IN_PARENT_RIGHT | POSITION_IN_PARENT_TOP;
	public static final int POSITION_IN_PARENT_BOTTOMRIGHT = POSITION_IN_PARENT_RIGHT | POSITION_IN_PARENT_BOTTOM;
	public static final int POSITION_IN_PARENT_BOTTOMLEFT = POSITION_IN_PARENT_LEFT | POSITION_IN_PARENT_BOTTOM;


	// ===========================================================
	// Fields
	// ===========================================================

	public final int x,y,zoom;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OSMTileInfo(final int x, final int y, final int zoom) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public OSMTileInfo getParentTile(){
		return new OSMTileInfo(this.x / 2, this.y / 2, this.zoom - 1);
	}

	/**
	 *
	 * @param child
	 * @param parent
	 * @return
	 */
	public int getPositionInParent(final OSMTileInfo pParent){
		final int childShouldUpperLeftX = pParent.x * 2;
		final int childShouldUpperLeftY = pParent.y * 2;

		int out = (childShouldUpperLeftX == this.x) ? POSITION_IN_PARENT_LEFT : POSITION_IN_PARENT_RIGHT;
		out += (childShouldUpperLeftY == this.y) ? POSITION_IN_PARENT_TOP : POSITION_IN_PARENT_BOTTOM;
		return out;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public int hashCode() {
		return ((x << 19) & 0xFFF80000) | ((y << 6) & 0x0007FFC0) | zoom;
	}

	@Override
	public boolean equals(final Object o) {
		if(o != null && o instanceof OSMTileInfo){
			final OSMTileInfo other = (OSMTileInfo)o;
			return other.x == x && other.y == y && other.zoom == zoom;
		}else{
			return super.equals(o);
		}
	}

	@Override
	public String toString(){
		return new StringBuilder()
			.append("z=").append(this.zoom)
			.append(" x=").append(this.x)
			.append(" y=").append(this.y)
			.toString();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

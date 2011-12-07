package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * A {@link BitmapDrawable} for a {@link MapTile} that has a state to indicate that it's expired.
 */
public class ExpiredBitmapDrawable extends BitmapDrawable {

	/** Tile is in cache but expired, so should go to first tile provider to retrieve again. */
	public static final int EXPIRED_IN_CACHE = -1;

	/** Tile is in filesystem but expired, so should go to next tile provider to retrieve again. */
	public static final int EXPIRED_IN_FILESYSTEM = -2;

	private int[] mState = new int[] { EXPIRED_IN_CACHE };

	public ExpiredBitmapDrawable(final Bitmap pBitmap) {
		super(pBitmap);
	}

	@Override
	public int[] getState() {
		return mState;
	}

	@Override
	public boolean isStateful() {
		return mState.length > 0;
	}

	@Override
	public boolean setState(final int[] pStateSet) {
		mState = pStateSet;
		return true;
	}

}

package org.osmdroid.tileprovider;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

public class LRUMapTileCache extends LinkedHashMap<MapTile, Drawable> {

	public interface TileRemovedListener {
		void onTileRemoved(MapTile mapTile);
	}

	private static final long serialVersionUID = -541142277575493335L;

	private int mCapacity;
	private TileRemovedListener mTileRemovedListener;

	public LRUMapTileCache(final int aCapacity) {
		super(aCapacity + 2, 0.1f, true);
		mCapacity = aCapacity;
	}

	public void ensureCapacity(final int aCapacity) {
		if (aCapacity > mCapacity) {
               Log.i(IMapView.LOGTAG, "Tile cache increased from " + mCapacity + " to " + aCapacity);
			mCapacity = aCapacity;
		}
	}

	@Override
	public Drawable remove(final Object aKey) {
		final Drawable drawable = super.remove(aKey);
		// Only recycle if we are running on a project less than 2.3.3 Gingerbread.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			if (drawable instanceof BitmapDrawable) {
				final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
		if (getTileRemovedListener() != null && aKey instanceof MapTile)
			getTileRemovedListener().onTileRemoved((MapTile) aKey);
		if (drawable instanceof ReusableBitmapDrawable)
			BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) drawable);
		return drawable;
	}

	@Override
	public void clear() {
		// remove them all individually so that they get recycled
		while (!isEmpty()) {
			try {
				remove(keySet().iterator().next());
			} catch (NoSuchElementException nse) {
				// as a protection
				//https://github.com/osmdroid/osmdroid/issues/776
			}
		}

		// and then clear
		super.clear();
	}

	@Override
	protected boolean removeEldestEntry(final java.util.Map.Entry<MapTile, Drawable> aEldest) {
		if (size() > mCapacity) {
			final MapTile eldest = aEldest.getKey();
			if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG,"LRU Remove old tile: " + eldest);
			}
			remove(eldest);
			// don't return true because we've already removed it
		}
		return false;
	}

	public TileRemovedListener getTileRemovedListener() {
		return mTileRemovedListener;
	}

	public void setTileRemovedListener(TileRemovedListener tileRemovedListener) {
		mTileRemovedListener = tileRemovedListener;
	}
}

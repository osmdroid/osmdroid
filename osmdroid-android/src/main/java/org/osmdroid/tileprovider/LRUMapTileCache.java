package org.osmdroid.tileprovider;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.MapTileList;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

@Deprecated
public class LRUMapTileCache extends LinkedHashMap<Long, Drawable> {

	public interface TileRemovedListener {
		void onTileRemoved(final long pMapTileIndex);
	}

	private static final long serialVersionUID = -541142277575493335L;

	private final MapTileList mMapTileList;
	private int mCapacity;
	private TileRemovedListener mTileRemovedListener;

	public LRUMapTileCache(final int aCapacity, final MapTileList pMapTileList) {
		super(aCapacity + 2, 0.1f, true);
		mCapacity = aCapacity;
		mMapTileList = pMapTileList;
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
		BitmapPool.getInstance().asyncRecycle(drawable);
		if (getTileRemovedListener() != null && aKey != null)
			getTileRemovedListener().onTileRemoved((long) aKey);
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
	protected boolean removeEldestEntry(final java.util.Map.Entry<Long, Drawable> aEldest) {
		final long index = aEldest.getKey();
		if (mMapTileList.contains(index)) {
			return false; // don't remove, it's a displayed tile
		}
		if (size() <= mCapacity) {
			return false; // don't remove, we are within the capacity
		}
		if (Configuration.getInstance().isDebugMode()) {
				Log.d(IMapView.LOGTAG,"LRU Remove old tile: " + MapTileIndex.toString(index));
		}
		return true; // remove
	}

	public TileRemovedListener getTileRemovedListener() {
		return mTileRemovedListener;
	}

	public void setTileRemovedListener(TileRemovedListener tileRemovedListener) {
		mTileRemovedListener = tileRemovedListener;
	}
}

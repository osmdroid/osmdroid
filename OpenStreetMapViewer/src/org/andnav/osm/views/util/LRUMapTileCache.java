package org.andnav.osm.views.util;

import java.util.LinkedHashMap;

import org.andnav.osm.services.util.OpenStreetMapTile;

import android.graphics.Bitmap;

public class LRUMapTileCache extends LinkedHashMap<OpenStreetMapTile, Bitmap> {

	private static final long serialVersionUID = -541142277575493335L;

	private int mCapacity;
	
	public LRUMapTileCache(final int aCapacity) {
		super(aCapacity + 2, 0.1f, true);
		mCapacity = aCapacity;
	}

	public void ensureCapacity(final int aCapacity) {
		if (aCapacity > mCapacity) {
			mCapacity = aCapacity;
		}
	}

	@Override
	public Bitmap remove(final Object aKey) {
		final Bitmap bm = super.remove(aKey);
		if (bm != null) {
			bm.recycle();
		}
		return bm;
	}

	@Override
	public void clear() {
		// remove them all individually so that they get recycled
		for(final OpenStreetMapTile key : keySet()) {
			remove(key);
		}

		// and then clear
		super.clear();
	}

	@Override
	protected boolean removeEldestEntry(final Entry<OpenStreetMapTile, Bitmap> aEldest) {
		return size() > mCapacity;
	}

}

package org.andnav.osm.views.util;

import java.util.LinkedHashMap;

import org.andnav.osm.services.util.OpenStreetMapTile;

import android.graphics.Bitmap;

public class LRUMapTileCache extends LinkedHashMap<OpenStreetMapTile, Bitmap> {

	private static final long serialVersionUID = -541142277575493335L;

	private final int mCapacity;
	
	public LRUMapTileCache(final int pCapacity) {
		super(pCapacity + 2, 0.1f, true);
		mCapacity = pCapacity;
	}

	@Override
	public Bitmap remove(Object pKey) {
		final Bitmap bm = super.remove(pKey);
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
	protected boolean removeEldestEntry(Entry<OpenStreetMapTile, Bitmap> pEldest) {
		return size() > mCapacity;
	}

}

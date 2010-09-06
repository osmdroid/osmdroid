package org.andnav.osm.views.util;

import java.io.File;
import java.util.Random;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.ResourceProxy.string;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

abstract class OpenStreetMapRendererBase implements IOpenStreetMapRendererInfo {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapRendererBase.class);

	private static int globalOrdinal = 0;

	private final int mOrdinal;
	private final String mName;
	private final ResourceProxy.string mResourceId;
	private final int mMaptileSizePx;
	private final int mMaptileZoom;
	private final int mZoomMinLevel;
	private final int mZoomMaxLevel;
	protected final String mImageFilenameEnding;
	private final String mBaseUrls[];
	int cloudmadeStyle = 1;
	protected final Random random = new Random();

	OpenStreetMapRendererBase(String aName, ResourceProxy.string aResourceId,
			int aZoomMinLevel, int aZoomMaxLevel,
			int aMaptileZoom,
			String aImageFilenameEnding, final String ...aBaseUrl) {
		mOrdinal = globalOrdinal++;
		mName = aName;
		mResourceId = aResourceId;
		mZoomMinLevel = aZoomMinLevel;
		mZoomMaxLevel = aZoomMaxLevel;
		mMaptileZoom = aMaptileZoom;
		mMaptileSizePx = 1 << aMaptileZoom;
		mImageFilenameEnding = aImageFilenameEnding;
		mBaseUrls = aBaseUrl;
	}

	@Override
	public int ordinal() {
		return mOrdinal;
	}

	@Override
	public String name() {
		return mName;
	}

	@Override
	public string resourceId() {
		return mResourceId;
	}

	@Override
	public int maptileSizePx() {
		return mMaptileSizePx;
	}

	@Override
	public int maptileZoom() {
		return mMaptileZoom;
	}

	@Override
	public int zoomMinLevel() {
		return mZoomMinLevel;
	}

	@Override
	public int zoomMaxLevel() {
		return mZoomMaxLevel;
	}

	@Override
	public String imageFilenameEnding() {
		return mImageFilenameEnding;
	}

	@Override
	public Drawable getDrawable(final String aFilePath) {
		try {
			// default implementation will load the file as a bitmap and create a BitmapDrawable from it
			final Bitmap bitmap = BitmapFactory.decodeFile(aFilePath);
			if (bitmap != null) {
				return new BitmapDrawable(bitmap);
			} else {
				// if we couldn't load it then it's invalid - delete it
				try {
					new File(aFilePath).delete();
				} catch (final Throwable e) {
					logger.error("Error deleting invalid file: " + aFilePath, e);
				}
			}
		} catch (final OutOfMemoryError e) {
			logger.error("OutOfMemoryError loading bitmap: " + aFilePath);
			System.gc();
		}
		return null;
	}

	/**
	 * Get the base url, which will be a random one if there are more than one.
	 */
	protected String getBaseUrl() {
		return mBaseUrls[random.nextInt(mBaseUrls.length)];
	}

}

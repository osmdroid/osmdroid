package org.osmdroid.tileprovider.tilesource;

public abstract class OnlineTileSourceBase extends BitmapTileSourceBase {

	private final String mBaseUrls[];

	/**
	 * Constructor
	 * @param aName a human-friendly name for this tile source
	 
	 * @param aZoomMinLevel the minimum zoom level this tile source can provide
	 * @param aZoomMaxLevel the maximum zoom level this tile source can provide
	 * @param aTileSizePixels the tile size in pixels this tile source provides
	 * @param aImageFilenameEnding the file name extension used when constructing the filename
	 * @param aBaseUrl the base url(s) of the tile server used when constructing the url to download the tiles
	 */
	public OnlineTileSourceBase(final String aName, 
			final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
			final String aImageFilenameEnding, final String[] aBaseUrl) {

		this(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding,aBaseUrl,null);

	}

	/**
	 * Constructor
	 * @param aName a human-friendly name for this tile source

	 * @param aZoomMinLevel the minimum zoom level this tile source can provide
	 * @param aZoomMaxLevel the maximum zoom level this tile source can provide
	 * @param aTileSizePixels the tile size in pixels this tile source provides
	 * @param aImageFilenameEnding the file name extension used when constructing the filename
	 * @param aBaseUrl the base url(s) of the tile server used when constructing the url to download the tiles
	 */
	public OnlineTileSourceBase(final String aName,
								final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
								final String aImageFilenameEnding, final String[] aBaseUrl, String copyyright) {
		super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
			aImageFilenameEnding, copyyright);
		mBaseUrls = aBaseUrl;
	}

	public abstract String getTileURLString(final long pMapTileIndex);

	/**
	 * Get the base url, which will be a random one if there are more than one.
	 */
	public String getBaseUrl() {
		return mBaseUrls[random.nextInt(mBaseUrls.length)];
	}
}

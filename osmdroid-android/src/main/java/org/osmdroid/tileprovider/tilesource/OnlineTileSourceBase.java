package org.osmdroid.tileprovider.tilesource;

import java.util.concurrent.Semaphore;

public abstract class OnlineTileSourceBase extends BitmapTileSourceBase {

    private final String[] mBaseUrls;

    /**
     * @since 6.1.0
     */
    private final Semaphore mSemaphore;

    /**
     * @since 6.1.0
     */
    private final TileSourcePolicy mTileSourcePolicy;

    public OnlineTileSourceBase(final String aName,
                                final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
                                final String aImageFilenameEnding, final String[] aBaseUrl) {

        this(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl, null);

    }

    public OnlineTileSourceBase(final String aName,
                                final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
                                final String aImageFilenameEnding, final String[] aBaseUrl, String copyyright) {
        this(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl, copyyright, new TileSourcePolicy());
    }

    /**
     * @param pName                a human-friendly name for this tile source
     * @param pZoomMinLevel        the minimum zoom level this tile source can provide
     * @param pZoomMaxLevel        the maximum zoom level this tile source can provide
     * @param pTileSizePixels      the tile size in pixels this tile source provides
     * @param pImageFilenameEnding the file name extension used when constructing the filename
     * @param pBaseUrl             the base url(s) of the tile server used when constructing the url to download the tiles
     * @param pCopyright           the source copyright
     * @param pTileSourcePolicy    tile source policy
     * @since 6.1.0
     */
    public OnlineTileSourceBase(final String pName,
                                final int pZoomMinLevel, final int pZoomMaxLevel, final int pTileSizePixels,
                                final String pImageFilenameEnding, final String[] pBaseUrl, final String pCopyright,
                                final TileSourcePolicy pTileSourcePolicy) {
        super(pName, pZoomMinLevel, pZoomMaxLevel, pTileSizePixels,
                pImageFilenameEnding, pCopyright);
        mBaseUrls = pBaseUrl;
        mTileSourcePolicy = pTileSourcePolicy;
        if (mTileSourcePolicy.getMaxConcurrent() > 0) {
            mSemaphore = new Semaphore(mTileSourcePolicy.getMaxConcurrent(), true);
        } else {
            mSemaphore = null;
        }
    }

    public abstract String getTileURLString(final long pMapTileIndex);

    /**
     * Get the base url, which will be a random one if there are more than one.
     * <br>
     * Updated around 6.1.1, if base url list is null or empty, empty string is returned
     */
    public String getBaseUrl() {
        if (mBaseUrls != null && mBaseUrls.length > 0)
            return mBaseUrls[random.nextInt(mBaseUrls.length)];
        return "";
    }

    /**
     * @since 6.1.0
     */
    public void acquire() throws InterruptedException {
        if (mSemaphore == null) {
            return;
        }
        mSemaphore.acquire();
    }

    /**
     * @since 6.1.0
     */
    public void release() {
        if (mSemaphore == null) {
            return;
        }
        mSemaphore.release();
    }

    /**
     * @since 6.1.0
     */
    public TileSourcePolicy getTileSourcePolicy() {
        return mTileSourcePolicy;
    }
}

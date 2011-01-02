package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CloudmadeTileSource extends OnlineTileSourceBase implements IStyledTileSource {

	private static final Logger logger = LoggerFactory.getLogger(CloudmadeTileSource.class);

	private String mKey;
	private String mToken;
	private int mStyle = 1;

	CloudmadeTileSource(final String aName, final ResourceProxy.string aResourceId,
			final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
			final String aImageFilenameEnding, final String... aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding, aBaseUrl);
	}

	@Override
	public String pathBase() {
		if (mStyle <= 1) {
			return mName;
		} else {
			return mName + mStyle;
		}
	}

	@Override
	public String getTileURLString(final MapTile aTile) {
		if (mKey == null) {
			mKey = CloudmadeUtil.getCloudmadeKey();
		}
		if (mToken == null) {
			synchronized (mKey) {
				if (mToken == null) {
					try {
						mToken = CloudmadeUtil.getCloudmadeToken(mKey);
					} catch (final CloudmadeException e) {
						return null;
					}
				}
			}
		}
		return String.format(getBaseUrl(), mKey, mStyle, getTileSizePixels(), aTile.getZoomLevel(),
				aTile.getX(), aTile.getY(), mImageFilenameEnding, mToken);
	}

	@Override
	public void setStyle(final int aStyle) {
		mStyle = aStyle;
	}

	@Override
	public void setStyle(final String aStyle) {
		try {
			mStyle = Integer.valueOf(aStyle);
		} catch (final NumberFormatException e) {
			logger.warn("Invalid style: " + aStyle);
		}
	}
}

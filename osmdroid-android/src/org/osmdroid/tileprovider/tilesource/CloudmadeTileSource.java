package org.osmdroid.tileprovider.tilesource;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.util.CloudmadeUtil;

class CloudmadeTileSource extends OnlineTileSourceBase implements IStyledTileSource<Integer> {

	private Integer mStyle = 1;

	CloudmadeTileSource(final String pName, final ResourceProxy.string pResourceId,
			final int pZoomMinLevel, final int pZoomMaxLevel, final int pTileSizePixels,
			final String pImageFilenameEnding, final String... pBaseUrl) {
		super(pName, pResourceId, pZoomMinLevel, pZoomMaxLevel, pTileSizePixels,
				pImageFilenameEnding, pBaseUrl);
	}

	@Override
	public String pathBase() {
		if (mStyle == null || mStyle <= 1) {
			return mName;
		} else {
			return mName + mStyle;
		}
	}

	@Override
	public String getTileURLString(final MapTile pTile) {
		final String key = CloudmadeUtil.getCloudmadeKey();
		final String token = CloudmadeUtil.getCloudmadeToken();
		return String.format(getBaseUrl(), key, mStyle, getTileSizePixels(), pTile.getZoomLevel(),
				pTile.getX(), pTile.getY(), mImageFilenameEnding, token);
	}

	@Override
	public void setStyle(final Integer pStyle) {
		mStyle = pStyle;
	}

	@Override
	public void setStyle(final String pStyle) {
		mStyle = Integer.getInteger(pStyle);
	}

	@Override
	public Integer getStyle() {
		return mStyle;
	}
}

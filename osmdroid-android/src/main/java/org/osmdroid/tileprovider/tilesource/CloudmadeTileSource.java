package org.osmdroid.tileprovider.tilesource;

import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.util.CloudmadeUtil;

public class CloudmadeTileSource extends OnlineTileSourceBase implements IStyledTileSource<Integer> {

	private Integer mStyle = 1;

	public CloudmadeTileSource(final String pName, 
			final int pZoomMinLevel, final int pZoomMaxLevel, final int pTileSizePixels,
			final String pImageFilenameEnding, final String[] pBaseUrl) {
		super(pName, pZoomMinLevel, pZoomMaxLevel, pTileSizePixels,
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
		if (key.length() == 0) {
			Log.e(IMapView.LOGTAG,"CloudMade key is not set. You should enter it in the manifest and call CloudmadeUtil.retrieveCloudmadeKey()");
		}
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
		try {
			mStyle = Integer.parseInt(pStyle);
		} catch (final NumberFormatException e) {
			Log.e(IMapView.LOGTAG,"Error setting integer style: " + pStyle);
		}
	}

	@Override
	public Integer getStyle() {
		return mStyle;
	}
}

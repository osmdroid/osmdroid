package org.osmdroid.tileprovider.tilesource.bing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import microsoft.mappoint.TileSystem;


import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.QuadTreeTileSource;
import org.osmdroid.tileprovider.tilesource.bing.imagerymetadata.ImageryMetaData;
import org.osmdroid.tileprovider.tilesource.bing.imagerymetadata.ImageryMetaDataResource;
import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.tileprovider.util.StreamUtils;

import android.content.Context;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import org.osmdroid.thirdparty.Constants;

/**
 * BingMap tile source used with OSMDroid<br>
 *
 * This class builds the Bing REST services url to be requested to get a tile image.<br>
 *
 * Before to be used, the static method {@link #retrieveBingKey} must be invoked.<br>
 *
 * See
 * <a href="http://msdn.microsoft.com/en-us/library/ff701721.aspx">http://msdn.microsoft.com/en-us/library/ff701721.aspx</a>
 * for details on the Bing API.
 */
public class BingMapTileSource extends QuadTreeTileSource implements IStyledTileSource<String> {

	/** the meta data key in the manifest */
	private static final String BING_KEY = "BING_KEY";

	//Constant used for imagerySet parameter
	/** Aerial imagery mode **/
	public static final String IMAGERYSET_AERIAL = "Aerial";
	/** Aerial imagery with road overlay mode **/
	public static final String IMAGERYSET_AERIALWITHLABELS = "AerialWithLabels";
	/** Roads imagery mode **/
	public static final String IMAGERYSET_ROAD = "Road";

	// Bing Map REST services return jpeg images
	private static final String FILENAME_ENDING =".jpeg";

	// URL used to get imageryData. It is requested in order to get tiles url patterns
	private static final String BASE_URL_PATTERN = "http://dev.virtualearth.net/REST/V1/Imagery/Metadata/%s?mapVersion=v1&output=json&key=%s";

	/** Bing Map key set by user.
	 * @see
	 * <a href="http://msdn.microsoft.com/en-us/library/ff428642.aspx">http://msdn.microsoft.com/en-us/library/ff428642.aspx</a>
	 */
	private static String mBingMapKey = "";

	private String mStyle = IMAGERYSET_ROAD;

	// object storing imagery meta data
	private ImageryMetaDataResource mImageryData = ImageryMetaDataResource.getDefaultInstance();

	// local used for set BingMap REST culture parameter
	private String mLocale;
	// baseURl used for OnlineTileSourceBase override
	private String mBaseUrl;
	// tile's image resolved url pattern
	private String mUrl;

	/**
	 * Constructor.<br> <b>Warning, the static method {@link #retrieveBingKey} should have been invoked once before constructor invocation</b>
	 * @param aLocale	The language used with BingMap REST service to retrieve tiles.<br> If null, the system default locale is used.
	 */
	public BingMapTileSource(final String aLocale) {
		super("BingMaps",  0, 22, 256, FILENAME_ENDING, null);
		mLocale = aLocale;
		if(mLocale==null) {
			mLocale=Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();
		}
	}

	/**
	 * Read the API key from the manifest.<br>
	 * This method should be invoked before class instantiation.<br>
	 */
	public static void retrieveBingKey(final Context aContext) {

		// get the key from the manifest
		mBingMapKey = ManifestUtil.retrieveKey(aContext, BING_KEY);
	}

	public static String getBingKey() {
		return mBingMapKey;
	}

	public static void setBingKey(String key) {
		mBingMapKey=key;
	}

	/*-------------- overrides OnlineTileSourceBase ---------------------*/

	@Override
	protected String getBaseUrl() {
		if (!mImageryData.m_isInitialised) {
			initMetaData();
		}
		return mBaseUrl;
	}

	@Override
	public String getTileURLString(final MapTile pTile)
	{
		if (!mImageryData.m_isInitialised) {
			initMetaData();
		}
		return String.format(mUrl,quadTree(pTile));
	}

	/**
	 * get minimum zoom level
	 * @return minimum zoom level supported by Bing Map for current map view mode
	 */
	@Override
	public int getMinimumZoomLevel() {
		return mImageryData.m_zoomMin;
	}
	/**
	 * get maximum zoom level
	 * @return maximum zoom level supported by Bing Map for current map view mode
	 */
	@Override
	public int getMaximumZoomLevel() {
		return mImageryData.m_zoomMax;
	}
	/**
	 * get tile size in pixel
	 * @return tile size in pixel supported by Bing Map for current map view mode
	 */
	@Override
	public int getTileSizePixels() {
		return mImageryData.m_imageHeight;
	}

	/**
	 * get the base path used for caching purpose
	 * @return a base path built on name given as constructor parameter and current style name
	 */
	@Override
	public String pathBase() {
		return mName + mStyle;
	}

	/*--------------- IStyledTileSource --------------------*/

	@Override
	/**
	 * Set the map style.
	 * @param aStyle The map style.<br>
	 * Should be one of {@link IMAGERYSET_AERIAL}, {@link IMAGERYSET_AERIALWITHLABELS} or {@link IMAGERYSET_ROAD}
	 */
	public void setStyle(final String pStyle) {
		if(!pStyle.equals(mStyle)) {
			// flag to re-read imagery data
			synchronized (mStyle) {
				mUrl = null;
				mBaseUrl = null;
				mImageryData.m_isInitialised = false;
			}
		}
		mStyle = pStyle;
	}

	@Override
	public String getStyle() {
		return mStyle;
	}

	private ImageryMetaDataResource initMetaData() {
		if (!mImageryData.m_isInitialised) {
			synchronized (this) {
				if (!mImageryData.m_isInitialised) {
					final ImageryMetaDataResource imageryData = getMetaData();
					if (imageryData != null) {
						mImageryData = imageryData;
						TileSystem.setTileSize(getTileSizePixels());
						updateBaseUrl();
					}
				}
			}
		}
		return mImageryData;
	}

	/**
	 * Gets the imagery meta from the REST service, or null if it fails
	 */
	private ImageryMetaDataResource getMetaData()
	{
		Log.d(Constants.LOGTAG,"getMetaData");

		
		          
		
 		HttpURLConnection client=null;
		try {
			client = (HttpURLConnection)(new URL(String.format(BASE_URL_PATTERN, mStyle, mBingMapKey)).openConnection());
			Log.d(Constants.LOGTAG,"make request "+client.getURL().toString().toString());
			client.setRequestProperty(OpenStreetMapTileProviderConstants.USER_AGENT, OpenStreetMapTileProviderConstants.getUserAgentValue());
			client.connect();

			if (client.getResponseCode()!= 200) {
				Log.e(Constants.LOGTAG,"Cannot get response for url "+client.getURL().toString() + " " + client.getResponseMessage());
				return null;
			}

			final InputStream in = client.getInputStream();
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			final BufferedOutputStream out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();

			return ImageryMetaData.getInstanceFromJSON(dataStream.toString());

		} catch(final Exception e) {
			Log.e(Constants.LOGTAG,"Error getting imagery meta data", e);
		} finally {
			try {
				if (client!=null)
					client.disconnect();
			} catch(Exception e) {

			}
			Log.d(Constants.LOGTAG,"end getMetaData");
		}
		return null;
	}

	/**
	 * Resolves url patterns to update urls with current map view mode and available sub domain.<br>
	 * When several subdomains are available, change current sub domain in a cycle manner
	 */
	protected void updateBaseUrl()
	{
		Log.d(Constants.LOGTAG,"updateBaseUrl");
		final String subDomain = mImageryData.getSubDomain();
		final int idx = mImageryData.m_imageUrl.lastIndexOf("/");
		if(idx>0) {
			mBaseUrl = mImageryData.m_imageUrl.substring(0,idx);
		} else {
			mBaseUrl = mImageryData.m_imageUrl;
		}

		mUrl = mImageryData.m_imageUrl;
		if(subDomain!=null)
		{
			mBaseUrl = String.format(mBaseUrl, subDomain);
			mUrl = String.format(mUrl, subDomain,"%s",mLocale);
		}
		Log.d(Constants.LOGTAG,"updated url = "+mUrl);
		Log.d(Constants.LOGTAG,"end updateBaseUrl");
	}

}

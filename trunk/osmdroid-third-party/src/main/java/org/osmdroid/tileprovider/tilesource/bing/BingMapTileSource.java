package org.osmdroid.tileprovider.tilesource.bing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import microsoft.mappoint.TileSystem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.QuadTreeTileSource;
import org.osmdroid.tileprovider.tilesource.bing.imagerymetadata.ImageryMetaData;
import org.osmdroid.tileprovider.tilesource.bing.imagerymetadata.ImageryMetaDataResource;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * BingMap tile source used with OSMDroid<br>
 *
 * This class builds the Bing REST services url to be requested to get a tile image.<br>
 *
 * Before to be used, the static method {@link retrieveBingKey} must be invoked.<br>
 *
 * See {@link http://msdn.microsoft.com/en-us/library/ff701721.aspx} for details on the Bing API.
 */
public class BingMapTileSource extends QuadTreeTileSource implements IStyledTileSource<String> {

	private static final Logger logger = LoggerFactory.getLogger(BingMapTileSource.class);

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
	 * See {@link http://msdn.microsoft.com/en-us/library/ff428642.aspx}
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
	 * Constructor.<br> <b>Warning, the static method {@link retrieveBingKey} should have been invoked once before constructor invocation</b>
	 * @param aLocale	The language used with BingMap REST service to retrieve tiles.<br> If null, the system default locale is used.
	 */
	public BingMapTileSource(final String aLocale) {
		super("BingMap", ResourceProxy.string.bing, -1, -1, -1, FILENAME_ENDING, (String)null);
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
		final PackageManager pm = aContext.getPackageManager();
		try {
			final ApplicationInfo info = pm.getApplicationInfo(aContext.getPackageName(),
					PackageManager.GET_META_DATA);
			if (info.metaData == null) {
				logger.info("Bing key not found in manifest");
			} else {
				final String key = info.metaData.getString(BING_KEY);
				if (key == null) {
					logger.info("Bing key not found in manifest");
				} else {
					if (DEBUGMODE) {
						logger.debug("Bing key: " + key);
					}
					mBingMapKey = key.trim();
				}
			}
		} catch (final NameNotFoundException e) {
			logger.info("Bing key not found in manifest", e);
		}
	}

	public static String getBingKey() {
		return mBingMapKey;
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
		logger.trace("getMetaData");

		final HttpClient client = new DefaultHttpClient();
		final HttpUriRequest head = new HttpGet(String.format(BASE_URL_PATTERN, mStyle, mBingMapKey));
		logger.debug("make request "+head.getURI().toString());
		try {
		    final HttpResponse response = client.execute(head);

		    final HttpEntity entity = response.getEntity();

		    if (entity == null) {
				logger.error("Cannot get response for url "+head.getURI().toString());
				return null;
			}

		    final InputStream in = entity.getContent();
		    final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		    final BufferedOutputStream out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();

			return ImageryMetaData.getInstanceFromJSON(dataStream.toString());

		} catch(final Exception e) {
			logger.error("Error getting imagery meta data", e);
		} finally {
			client.getConnectionManager().shutdown();
			logger.trace("end getMetaData");
		}
		return null;
	}

	/**
	 * Resolves url patterns to update urls with current map view mode and available sub domain.<br>
	 * When several subdomains are available, change current sub domain in a cycle manner
	 */
	protected void updateBaseUrl()
	{
		logger.trace("updateBaseUrl");
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
		logger.debug("updated url = "+mUrl);
		logger.trace("end updateBaseUrl");
	}

}

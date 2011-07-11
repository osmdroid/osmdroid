package org.osmdroid.tileprovider.tilesource.bing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

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
 * This class builds the Bing REST services url to be requested to get a tile image<br>
 *
 *  Before to be used, the static initMetaData method must be invoke to get url patterns dynamically
 *
 */
public class BingMapTileSource extends QuadTreeTileSource implements IStyledTileSource<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(BingMapTileSource.class);

	/** the meta data key in the manifest */
	private static final String BING_KEY = "BING_KEY";

	/** Bing Map key set by user.
	 * See http://msdn.microsoft.com/en-us/library/ff428642.aspx
	 */
	private static String BING_MAP_KEY = "";

	//Constant used for imagerySet parameter
	/** Aerial imagery mode **/
	public static final int IMAGERYSET_AERIAL = 1; // – Aerial imagery.
	/** Aerial imagery with road overlay mode **/
	public static final int IMAGERYSET_AERIALWITHLABELS = 2; // –Aerial imagery with a road overlay.
	/** Roads imagery mode **/
	public static final int IMAGERYSET_ROAD = 3; // – Roads without additional imagery.

	private final String[] m_imagerySet = {"Aerial", "AerialWithLabels", "Road"};

	private Integer m_style = IMAGERYSET_ROAD;

	// Bing Map REST services return jpeg images
	private static final String FILENAME_ENDING =".jpeg";

	// URL used to get imageryData. It is requested in order to get tiles url patterns
	private static final String BASE_URL_PATTERN = "http://dev.virtualearth.net/REST/V1/Imagery/Metadata/%s?mapVersion=v1&output=json&key=%s";

	// objects storing imagery meta data. One for each map view mode
	private static ImageryMetaDataResource s_roadMetaData;
	private static ImageryMetaDataResource s_aerialMetaData;
	private static ImageryMetaDataResource s_aerialwithLabelsMetaData;

	// baseURl used for OnlineTileSourceBase override
	private String m_baseUrl;
	// local used for set BingMap REST culture parameter
	private String m_locale;
	// tile's image resolved url pattern
	private String m_url;

	/**
	 * Constructor.<br> <b>Warning, the static method {@link initMetaData} should have been invoked once before constructor invocation</b>
	 * @param pName	A name associated to the current BingMapTileSource
	 * @param a_locale	The language used with BingMap REST service to retrieve tiles.<br> If null, the default locale is used.
	 */
	public BingMapTileSource(final String pName, final String a_locale) {
		super(pName, ResourceProxy.string.bing, -1, -1, -1, FILENAME_ENDING, (String)null);
		m_locale = a_locale;
		if(m_locale==null) {
			m_locale=Locale.getDefault().getISO3Language()+"-"+Locale.getDefault().getISO3Language();
		}
		updateBaseUrl();
	}

	/**
	 * Initialize BingMap tile source.<br>
	 * This method should be invoked before class instantiation.<br>
	 * It get dynamically the REST service url to be used to get tile in each supported map view mode.
	 * @param a_BingMapKey	The user's BingMap key.
	 * @throws Exception
	 */
	public static synchronized void initMetaData(final Context aContext) throws Exception
	{
		logger.trace("initMetaData");

		retrieveBingKey(aContext);

		s_roadMetaData = null;
		s_aerialMetaData = null;
		s_aerialwithLabelsMetaData = null;
		// get imageryData using the base url and input BingMap key
		// ImageryData is get for  each supported mode.

		// Roads mode
		final HttpClient client = new DefaultHttpClient();
		HttpUriRequest head = new HttpGet(String.format(BASE_URL_PATTERN, "Road", BING_MAP_KEY));
		logger.debug("make request "+head.getURI().toString());
	    HttpResponse response = client.execute(head);

        HttpEntity entity = response.getEntity();

        if (entity == null) {
			throw new Exception("Cannot get response for url "+head.getURI().toString());
		}

        InputStream in = entity.getContent();
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
		StreamUtils.copy(in, out);
		out.flush();

		s_roadMetaData = ImageryMetaData.getInstanceFromJSON(dataStream.toString());

		// Aerial mode
		head = new HttpGet(String.format(BASE_URL_PATTERN, "Aerial", BING_MAP_KEY));
		logger.debug("make request "+head.getURI().toString());
		response = client.execute(head);
        entity = response.getEntity();

        if (entity == null) {
			throw new Exception("Cannot get response for url "+head.getURI().toString());
		}

        in = entity.getContent();
        dataStream = new ByteArrayOutputStream();
        out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
		StreamUtils.copy(in, out);
		out.flush();

		s_aerialMetaData = ImageryMetaData.getInstanceFromJSON(dataStream.toString());

		// mixed mode
		head = new HttpGet(String.format(BASE_URL_PATTERN, "AerialWithLabels", BING_MAP_KEY));
		logger.debug("make request "+head.getURI().toString());
		response = client.execute(head);
        entity = response.getEntity();

        if (entity == null) {
			throw new Exception("Cannot get response for url "+head.getURI().toString());
		}

        in = entity.getContent();
        dataStream = new ByteArrayOutputStream();
        out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
		StreamUtils.copy(in, out);
		out.flush();

		s_aerialwithLabelsMetaData = ImageryMetaData.getInstanceFromJSON(dataStream.toString());

		client.getConnectionManager().shutdown();
		logger.trace("end initMetaData");
	}

	/**
	 * Retrieve the key from the manifest and store it for later use.
	 */
	public static void retrieveBingKey(final Context pContext) throws NameNotFoundException {

		// get the key from the manifest
		final PackageManager pm = pContext.getPackageManager();
		try {
			final ApplicationInfo info = pm.getApplicationInfo(pContext.getPackageName(),
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
					BING_MAP_KEY = key.trim();
				}
			}
		} catch (final NameNotFoundException e) {
			logger.info("Bing key not found in manifest", e);
			throw e;
		}
	}

	/**
	 * Return the ImageryMetaDataResource object associated to the input style
	 * @param a_style	The input style
	 * @return	the associated ImageryMetaDataResource or null if input style is not valid
	 */
	private static ImageryMetaDataResource getStyledImageryDataResource(final int a_style) {
		switch (a_style) {
		case IMAGERYSET_AERIAL:
			return s_aerialMetaData;
		case IMAGERYSET_AERIALWITHLABELS:
			return s_aerialwithLabelsMetaData;
		case IMAGERYSET_ROAD:
			return s_roadMetaData;
		default:
			return null;
		}
	}

	/**
	 * Resolves url patterns to update urls with current map view mode and available sub domain.<br>
	 * When several subdomains are available, change current sub domain in a cycle manner
	 */
	protected void updateBaseUrl()
	{
		logger.trace("updateBaseUrl");
		final ImageryMetaDataResource resource = getStyledImageryDataResource(m_style);
		final String subDomain = resource.getSubDomain();
		final int idx = resource.m_imageUrl.lastIndexOf("/");
		if(idx>0) {
			m_baseUrl = resource.m_imageUrl.substring(0,idx);
		} else {
			m_baseUrl = resource.m_imageUrl;
		}

		m_url = resource.m_imageUrl;
		if(subDomain!=null)
		{
			m_baseUrl = String.format(m_baseUrl, subDomain);
			m_url = String.format(m_url, subDomain,"%s",m_locale);
		}
		logger.debug("updated url = "+m_url);
		logger.trace("end updateBaseUrl");
	}

	/*-------------- overrides OnlineTileSourceBase ---------------------*/

	/**
	 * get base url
	 * @return the current base url
	 */
	@Override
	protected String getBaseUrl() {
		return m_baseUrl;
	}

	/**
	 * get the url to invoke to retrieve image for input tile
	 * @param pTile the input tile
	 * @return the associated url
	 */
	@Override
	public String getTileURLString(final MapTile pTile)
	{
		return String.format(m_url,quadTree(pTile));
		//String url =String.format(m_url,quadTree(pTile));
		//Log.d(TAG,"make url = "+url);
		//return url;
	}

	/**
	 * get minimum zoom level
	 * @return minimum zoom level supported by Bing Map for current map view mode
	 */
	@Override
	public int getMinimumZoomLevel() {
		return getStyledImageryDataResource(m_style).m_zoomMin;
	}
	/**
	 * get maximum zoom level
	 * @return maximum zoom level supported by Bing Map for current map view mode
	 */
	@Override
	public int getMaximumZoomLevel() {
		return getStyledImageryDataResource(m_style).m_zoomMax;
	}
	/**
	 * get tile size in pixel
	 * @return tile size in pixel supported by Bing Map for current map view mode
	 */
	@Override
	public int getTileSizePixels() {
		return getStyledImageryDataResource(m_style).m_imageHeight;
	}

	/**
	 * get the base path used for caching purpose
	 * @return a base path built on name given as constructor parameter and current style name
	 */
	@Override
	public String pathBase() {
		return mName +getStyleName();
	}

	/*--------------- IStyledTileSource --------------------*/

	@Override
	public void setStyle(final Integer pStyle) {
		boolean updateBaseUrl = false;
		if(m_style.intValue()!=pStyle.intValue()) {
			updateBaseUrl = true;
		}
		m_style = pStyle;
		// mode has been change, url pattern resolution should be updated
		if(updateBaseUrl) {
			updateBaseUrl();
		}
	}

	@Override
	public void setStyle(final String pStyle) {
		final Integer oldStyle = m_style;
		m_style = Integer.getInteger(pStyle);
		// mode has been change, url pattern resolution should be updated
		if(m_style.intValue()!=oldStyle.intValue()) {
			updateBaseUrl();
		}
	}

	@Override
	public Integer getStyle() {
		return m_style;
	}

	/**
	 * get current style name
	 * @return name associated to the current map view mode
	 */
	private String getStyleName() {
		if (m_style == null || m_style < 1 || m_style>m_imagerySet.length) {
			return "";
		} else {
			return m_imagerySet[m_style-1];
		}
	}

}

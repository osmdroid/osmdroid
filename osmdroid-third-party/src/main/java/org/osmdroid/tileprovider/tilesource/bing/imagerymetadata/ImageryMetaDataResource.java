package org.osmdroid.tileprovider.tilesource.bing.imagerymetadata;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ImageryMetaData storage. Class used to parse and store useful ImageryMetaData fields.
 *
 */
public class ImageryMetaDataResource {

	// Useful fields
	private final static String IMAGE_WIDTH = "imageWidth";
	private final static String IMAGE_HEIGHT = "imageHeight";
	private final static String IMAGE_URL = "imageUrl";
	private final static String IMAGE_URL_SUBDOMAINS = "imageUrlSubdomains";
	private final static String ZOOM_MIN = "ZoomMin";
	private final static String ZOOM_MAX = "ZoomMax";

	/** image height in pixels (256 as default value) **/
	public int m_imageHeight=256;
	/** image width in pixels (256 as default value) **/
	public int m_imageWidth=256;
	/** image url pattern **/
	public String m_imageUrl;
	/** list of available sub domains. Can be null. **/
	public String[] m_imageUrlSubdomains;
	/** maximum zoom level (22 as default value for BingMap) **/
	public int m_zoomMax=22;
	/** minimum zoom level (1 as default value for BingMap) **/
	public int m_zoomMin=1;
	/** whether this imagery has been initialised */
	public boolean m_isInitialised = false;

	// counter used to manage next available sub domain
	private int m_subdomainsCounter = 0;

	/**
	 * Get an instance with default values.
	 * @return
	 */
	static public ImageryMetaDataResource getDefaultInstance() {
		return new ImageryMetaDataResource();
	}

	/**
	 * Parse a JSON string containing resource field of a ImageryMetaData response
	 * @param a_jsonObject	the JSON content string
	 * @return	ImageryMetaDataResource object containing parsed information
	 * @throws Exception
	 */
	static public ImageryMetaDataResource getInstanceFromJSON(final JSONObject a_jsonObject) throws Exception
	{
		final ImageryMetaDataResource result = new ImageryMetaDataResource();

		if(a_jsonObject==null) {
			throw new Exception("JSON to parse is null");
		}

		if(a_jsonObject.has(IMAGE_HEIGHT)) {
			result.m_imageHeight = a_jsonObject.getInt(IMAGE_HEIGHT);
		}
		if(a_jsonObject.has(IMAGE_WIDTH)) {
			result.m_imageWidth = a_jsonObject.getInt(IMAGE_WIDTH);
		}
		if(a_jsonObject.has(ZOOM_MIN)) {
			result.m_zoomMin = a_jsonObject.getInt(ZOOM_MIN);
		}
		if(a_jsonObject.has(ZOOM_MAX)) {
			result.m_zoomMax = a_jsonObject.getInt(ZOOM_MAX);
		}
		result.m_imageUrl = a_jsonObject.getString(IMAGE_URL);
		if(result.m_imageUrl!=null && result.m_imageUrl.matches(".*?\\{.*?\\}.*?")) {
			result.m_imageUrl = result.m_imageUrl.replaceAll("\\{.*?\\}", "%s");
		}

		final JSONArray subdomains = a_jsonObject.getJSONArray(IMAGE_URL_SUBDOMAINS);
		if(subdomains!=null && subdomains.length()>=1)
		{
			result.m_imageUrlSubdomains = new String[subdomains.length()];
			for(int i=0;i<subdomains.length();i++)
			{
				result.m_imageUrlSubdomains[i] = subdomains.getString(i);
			}

		}

		result.m_isInitialised = true;

		return result;
	}

	/**
	 * When several subdomains are available, get subdomain pointed by internal cycle counter on subdomains and increment this counter
	 * @return	the subdomain string associated to current counter value.
	 */
	public synchronized String getSubDomain()
	{
		if(m_imageUrlSubdomains==null || m_imageUrlSubdomains.length<=0) {
			return null;
		}

		final String result = m_imageUrlSubdomains[m_subdomainsCounter];
		if(m_subdomainsCounter<m_imageUrlSubdomains.length-1) {
			m_subdomainsCounter++;
		} else {
			m_subdomainsCounter=0;
		}

		return result;
	}
}

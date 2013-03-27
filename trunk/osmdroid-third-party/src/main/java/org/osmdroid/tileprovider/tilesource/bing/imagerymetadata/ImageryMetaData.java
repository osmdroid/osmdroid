package org.osmdroid.tileprovider.tilesource.bing.imagerymetadata;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ImageryMetaData storage. Class used to decode valid ImageryMetaData.
 *
 */
public class ImageryMetaData {

	// Useful fields found in ImageryMetaData response
	private final static String STATUS_CODE="statusCode";
	private final static String AUTH_RESULT_CODE="authenticationResultCode";
	private final static String AUTH_RESULT_CODE_VALID="ValidCredentials";
	private final static String RESOURCE_SETS="resourceSets";
	private final static String ESTIMATED_TOTAL="estimatedTotal";
	private final static String RESOURCE = "resources";

	/**
	 * Parse a JSON string containing ImageryMetaData response
	 * @param a_jsonContent	the JSON content string
	 * @return	ImageryMetaDataResource object containing parsed information
	 * @throws Exception
	 */
	static public ImageryMetaDataResource getInstanceFromJSON(final String a_jsonContent) throws Exception
	{

		if(a_jsonContent==null) {
			throw new Exception("JSON to parse is null");
		}

		/// response code should be 200 and authorization should be valid (valid BingMap key)
		final JSONObject jsonResult = new JSONObject(a_jsonContent);
		final int statusCode = jsonResult.getInt(STATUS_CODE);
		if(statusCode!=200) {
			throw new Exception("Status code = "+statusCode);
		}

		if(AUTH_RESULT_CODE_VALID.compareToIgnoreCase(jsonResult.getString(AUTH_RESULT_CODE))!=0) {
			throw new Exception("authentication result code = "+jsonResult.getString(AUTH_RESULT_CODE));
		}

		// get first valid resource information
		final JSONArray resultsSet = jsonResult.getJSONArray(RESOURCE_SETS);
		if(resultsSet==null || resultsSet.length()<1) {
			throw new Exception("No results set found in json response");
		}

		if(resultsSet.getJSONObject(0).getInt(ESTIMATED_TOTAL)<=0) {
			throw new Exception("No resource found in json response");
		}

		final JSONObject resource = resultsSet.getJSONObject(0).getJSONArray(RESOURCE).getJSONObject(0);

		return ImageryMetaDataResource.getInstanceFromJSON(resource);
	}

}

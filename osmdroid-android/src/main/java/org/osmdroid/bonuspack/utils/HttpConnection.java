package org.osmdroid.bonuspack.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import android.util.Log;

/**
 * A "very very simple to use" class for performing http get and post requests. 
 * So many ways to do that, and potential subtle issues. 
 * If complexity should be added to handle even more issues, complexity should be put here and only here. 
 * 
 * Typical usage: 
 * <pre>HttpConnection connection = new HttpConnection();
 * connection.doGet("http://www.google.com");
 * InputStream stream = connection.getStream();
 * if (stream != null) {
 * 	//use this stream, for buffer reading, or XML SAX parsing, or whatever... 
 * }
 * connection.close();</pre>
 */
public class HttpConnection {

	private DefaultHttpClient client;
	private InputStream stream;
	private HttpEntity entity;
	private String mUserAgent;
	
	private final static int TIMEOUT_CONNECTION=3000; //ms 
	private final static int TIMEOUT_SOCKET=10000; //ms
	
	public HttpConnection(){
		stream = null;
		entity = null;
		HttpParams httpParameters = new BasicHttpParams();
		/* useful?
		HttpProtocolParams.setContentCharset(httpParameters, "UTF-8"); 
		HttpProtocolParams.setHttpElementCharset(httpParameters, "UTF-8");
		*/
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_CONNECTION);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_SOCKET);
		client = new DefaultHttpClient(httpParameters); 
			//TODO: created here. Reuse to do for better perfs???...
	}
	
	public void setUserAgent(String userAgent){
		mUserAgent = userAgent;
	}
	
	/**
	 * @param sUrl url to get
	 */
	public void doGet(String sUrl){
		try {
			HttpGet request = new HttpGet(sUrl);
			if (mUserAgent != null)
				request.setHeader("User-Agent", mUserAgent);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != 200) {
				Log.e(BonusPackHelper.LOG_TAG, "Invalid response from server: " + status.toString());
			} else {
				entity = response.getEntity();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void doPost(String sUrl, List<NameValuePair> nameValuePairs) {
		try {
			HttpPost request = new HttpPost(sUrl);
			if (mUserAgent != null)
				request.setHeader("User-Agent", mUserAgent);
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != 200) {
				Log.e(BonusPackHelper.LOG_TAG, "Invalid response from server: " + status.toString());
			} else {
				entity = response.getEntity();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * @return the opened InputStream, or null if creation failed for any reason. 
	 */
	public InputStream getStream() {
		try {
			if (entity != null)
				stream = entity.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	/**
	 * @return the whole content as a String, or null if creation failed for any reason. 
	 */
	public String getContentAsString(){
		try {
			if (entity != null) {
				return EntityUtils.toString(entity, "UTF-8");
					//setting the charset is important if none found in the entity. 
			} else 
				return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Calling close once is mandatory. 
	 */
	public void close(){
		if (stream != null){
			try { 
				stream.close();
				stream = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (entity != null){
			try {
				entity.consumeContent(); 
					//"finish". Important if we want to reuse the client object one day... 
				entity = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (client != null){
			client.getConnectionManager().shutdown();
			client = null;
		}
	}
	
}

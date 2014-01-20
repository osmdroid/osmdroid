package org.osmdroid.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

/**
 * Factory class for creating an instance of {@link HttpClient}.
 * The default implementation returns an instance of {@link DefaultHttpClient}.
 * In order to use a different implementation call {@link #setFactoryInstance(IHttpClientFactory)}
 * early in your code, for example in <code>onCreate</code> in your main activity.
 * For example to use
 * <a href="http://square.github.io/okhttp/">OkHttp/</a>
 * use the following code
 * <code>
 * HttpClientFactory.setFactoryInstance(new IHttpClientFactory() {
 *     public HttpClient createHttpClient() {
 *         return new OkApacheClient();
 *     }
 * });
 * </code>
 */
public class HttpClientFactory {

	private static IHttpClientFactory mFactoryInstance = new IHttpClientFactory() {
		@Override
		public HttpClient createHttpClient() {
			final DefaultHttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "osmdroid");
			return client;
		}
	};

	public static void setFactoryInstance(final IHttpClientFactory aHttpClientFactory) {
		mFactoryInstance = aHttpClientFactory;
	}

	public static HttpClient createHttpClient() {
		return mFactoryInstance.createHttpClient();
	}

}

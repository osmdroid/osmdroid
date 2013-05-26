package org.osmdroid.http;

/**
 * Factory class for creating an instance of {@link org.apache.http.client.HttpClient}.
 */
public class HttpClientFactory {

	private static IHttpClientFactory mInstance = new ApacheHttpClientFactory();

	public static IHttpClientFactory getInstance() {
		return mInstance;
	}

	public static void setInstance(final IHttpClientFactory aHttpClientFactory) {
		mInstance = aHttpClientFactory;
	}

}

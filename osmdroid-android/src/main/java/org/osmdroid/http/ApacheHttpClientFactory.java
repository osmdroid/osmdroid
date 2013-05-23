package org.osmdroid.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * An implementation of {@link IHttpClientFactory} that uses the default Apache implementation.
 */
public class ApacheHttpClientFactory implements IHttpClientFactory {

    @Override
    public HttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

}

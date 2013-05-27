package org.osmdroid.http;

import org.apache.http.client.HttpClient;

/**
 * Factory class for creating an instance of {@link HttpClient}.
 * See {@link HttpClientFactory} for usage.
 */
public interface IHttpClientFactory {

    /**
     * Create an instance of {@link HttpClient}.
     */
    HttpClient createHttpClient();

}

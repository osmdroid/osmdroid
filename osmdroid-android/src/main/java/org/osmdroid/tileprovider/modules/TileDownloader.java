package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.MapTileIndex;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
public class TileDownloader {

    private boolean compatibilitySocketFactorySet;

    public Drawable downloadTile(final long pMapTileIndex,
                                 final IFilesystemCache pFilesystemCache, final OnlineTileSourceBase pTileSource) throws CantContinueException {
        return downloadTile(pMapTileIndex, 0, pTileSource.getTileURLString(pMapTileIndex), pFilesystemCache, pTileSource);
    }

    /**
     * downloads a tile and follows http redirects
     * Code used to be in MapTileDownloader.TileLoader.downloadTile
     */
    public Drawable downloadTile(final long pMapTileIndex, final int redirectCount, final String targetUrl,
                                 final IFilesystemCache pFilesystemCache, final OnlineTileSourceBase pTileSource) throws CantContinueException {

        // prevent infinite looping of redirects, rare but very possible for misconfigured servers
        if (redirectCount > 3) {
            return null;
        }

        String userAgent = null;
        if (pTileSource.getTileSourcePolicy().normalizesUserAgent()) {
            userAgent = Configuration.getInstance().getNormalizedUserAgent();
        }
        if (userAgent == null) {
            userAgent = Configuration.getInstance().getUserAgentValue();
        }
        if (!pTileSource.getTileSourcePolicy().acceptsUserAgent(userAgent)) {
            Log.e(IMapView.LOGTAG, "Please configure a relevant user agent; current value is: " + userAgent);
            return null;
        }
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection c = null;
        ByteArrayInputStream byteStream = null;
        ByteArrayOutputStream dataStream = null;
        try {
            final String tileURLString = targetUrl;

            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Downloading Maptile from url: " + tileURLString);
            }

            if (TextUtils.isEmpty(tileURLString)) {
                return null;
            }

            // Try to enable TLSv1.2 and/or disable SSLv3 on older devices
            // see:
            // https://stackoverflow.com/questions/33567596/android-https-web-service-communication-ssl-tls-1-2/33567745#33567745
            // https://stackoverflow.com/questions/26649389/how-to-disable-sslv3-in-android-for-httpsurlconnection#29946540
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && !compatibilitySocketFactorySet) {
                SSLSocketFactory socketFactory = new CompatibilitySocketFactory(
                        HttpsURLConnection.getDefaultSSLSocketFactory());
                HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
                compatibilitySocketFactorySet = true;
            }

            if (Configuration.getInstance().getHttpProxy() != null) {
                c = (HttpURLConnection) new URL(tileURLString).openConnection(Configuration.getInstance().getHttpProxy());
            } else {
                c = (HttpURLConnection) new URL(tileURLString).openConnection();
            }
            c.setUseCaches(true);
            c.setRequestProperty(Configuration.getInstance().getUserAgentHttpHeader(), userAgent);
            for (final Map.Entry<String, String> entry : Configuration.getInstance().getAdditionalHttpRequestProperties().entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }
            c.connect();

            // Check to see if we got success
            if (c.getResponseCode() != 200) {
                switch (c.getResponseCode()) {
                    case 301:
                    case 302:
                    case 307:
                    case 308:
                        if (Configuration.getInstance().isMapTileDownloaderFollowRedirects()) {
                            //this is a redirect, check the header for a 'Location' header
                            String redirectUrl = c.getHeaderField("Location");
                            if (redirectUrl != null) {
                                if (redirectUrl.startsWith("/")) {
                                    //in this case we need to stitch together a full url
                                    URL old = new URL(targetUrl);
                                    int port = old.getPort();
                                    boolean secure = targetUrl.toLowerCase().startsWith("https://");
                                    if (port == -1)
                                        if (targetUrl.toLowerCase().startsWith("http://")) {
                                            port = 80;
                                        } else {
                                            port = 443;
                                        }

                                    redirectUrl = (secure ? "https://" : "http") + old.getHost() + ":" + port + redirectUrl;
                                }
                                Log.i(IMapView.LOGTAG, "Http redirect for MapTile: " + MapTileIndex.toString(pMapTileIndex) + " HTTP response: " + c.getResponseMessage() + " to url " + redirectUrl);
                                return downloadTile(pMapTileIndex, redirectCount + 1, redirectUrl, pFilesystemCache, pTileSource);
                            }
                            break;
                        }    //else follow through the normal path of aborting the download
                    default: {
                        Log.w(IMapView.LOGTAG, "Problem downloading MapTile: " + MapTileIndex.toString(pMapTileIndex) + " HTTP response: " + c.getResponseMessage());
                        if (Configuration.getInstance().isDebugMapTileDownloader()) {
                            Log.d(IMapView.LOGTAG, tileURLString);
                        }
                        Counters.tileDownloadErrors++;
                        in = c.getErrorStream(); // in order to have the error stream purged by the finally block
                        return null;
                    }
                }
            }

            String mime = c.getHeaderField("Content-Type");
            if (Configuration.getInstance().isDebugMapTileDownloader()) {
                Log.d(IMapView.LOGTAG, tileURLString + " success, mime is " + mime);
            }
            if (mime != null && !mime.toLowerCase().contains("image")) {
                Log.w(IMapView.LOGTAG, tileURLString + " success, however the mime type does not appear to be an image " + mime);
            }

            in = c.getInputStream();

            dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
            final long expirationTime = pTileSource.getTileSourcePolicy().computeExpirationTime(
                    c, System.currentTimeMillis());
            StreamUtils.copy(in, out);
            out.flush();
            final byte[] data = dataStream.toByteArray();
            byteStream = new ByteArrayInputStream(data);

            // Save the data to the cache
            // this is the only point in which we insert tiles to the db or local file system.
            if (pFilesystemCache != null) {
                pFilesystemCache.saveFile(pTileSource, pMapTileIndex, byteStream, expirationTime);
                byteStream.reset();
            }
            return pTileSource.getDrawable(byteStream);
        } catch (final UnknownHostException e) {
            Log.w(IMapView.LOGTAG, "UnknownHostException downloading MapTile: " + MapTileIndex.toString(pMapTileIndex) + " : " + e);
            Counters.tileDownloadErrors++;
        } catch (final BitmapTileSourceBase.LowMemoryException e) {
            // low memory so empty the queue
            Counters.countOOM++;
            Log.w(IMapView.LOGTAG, "LowMemoryException downloading MapTile: " + MapTileIndex.toString(pMapTileIndex) + " : " + e);
            throw new CantContinueException(e);
        } catch (final FileNotFoundException e) {
            Counters.tileDownloadErrors++;
            Log.w(IMapView.LOGTAG, "Tile not found: " + MapTileIndex.toString(pMapTileIndex) + " : " + e);
        } catch (final IOException e) {
            Counters.tileDownloadErrors++;
            Log.w(IMapView.LOGTAG, "IOException downloading MapTile: " + MapTileIndex.toString(pMapTileIndex) + " : " + e);
        } catch (final Throwable e) {
            Counters.tileDownloadErrors++;
            Log.e(IMapView.LOGTAG, "Error downloading MapTile: " + MapTileIndex.toString(pMapTileIndex), e);
        } finally {
            StreamUtils.closeStream(in);
            StreamUtils.closeStream(out);
            StreamUtils.closeStream(byteStream);
            StreamUtils.closeStream(dataStream);
            try {
                c.disconnect();
            } catch (Exception ex) {
            }
        }

        return null;
    }

    /**
     * @return the Epoch timestamp corresponding to the http header (in milliseconds), or null
     * @since 6.0.3
     * @deprecated Use {@link TileSourcePolicy#getHttpExpiresTime(String)} instead
     */
    @Deprecated
    public Long getHttpExpiresTime(final String pHttpExpiresHeader) {
        if (pHttpExpiresHeader != null && pHttpExpiresHeader.length() > 0) {
            try {
                final Date dateExpires = Configuration.getInstance().getHttpHeaderDateTimeFormat().parse(pHttpExpiresHeader);
                return dateExpires.getTime();
            } catch (final Exception ex) {
                if (Configuration.getInstance().isDebugMapTileDownloader())
                    Log.d(IMapView.LOGTAG, "Unable to parse expiration tag for tile, server returned " + pHttpExpiresHeader, ex);
            }
        }
        return null;
    }

    /**
     * @return the max-age corresponding to the http header (in seconds), or null
     * @since 6.0.3
     * @deprecated Use {@link TileSourcePolicy#getHttpCacheControlDuration(String)} instead
     */
    @Deprecated
    public Long getHttpCacheControlDuration(final String pHttpCacheControlHeader) {
        if (pHttpCacheControlHeader != null && pHttpCacheControlHeader.length() > 0) {
            try {
                final String[] parts = pHttpCacheControlHeader.split(", ");
                final String maxAge = "max-age=";
                for (final String part : parts) {
                    final int pos = part.indexOf(maxAge);
                    if (pos == 0) {
                        final String durationString = part.substring(maxAge.length());
                        return Long.valueOf(durationString);
                    }
                }
            } catch (final Exception ex) {
                if (Configuration.getInstance().isDebugMapTileDownloader())
                    Log.d(IMapView.LOGTAG,
                            "Unable to parse cache control tag for tile, server returned " + pHttpCacheControlHeader, ex);
            }
        }
        return null;
    }

    /**
     * @return the expiration time (as Epoch timestamp in milliseconds)
     * @since 6.0.3
     * @deprecated Use {@link TileSourcePolicy#computeExpirationTime(HttpURLConnection, long)} instead
     */
    @Deprecated
    public long computeExpirationTime(final String pHttpExpiresHeader, final String pHttpCacheControlHeader, final long pNow) {
        final Long override = Configuration.getInstance().getExpirationOverrideDuration();
        if (override != null) {
            return pNow + override;
        }

        final long extension = Configuration.getInstance().getExpirationExtendedDuration();
        final Long cacheControlDuration = getHttpCacheControlDuration(pHttpCacheControlHeader);
        if (cacheControlDuration != null) {
            return pNow + cacheControlDuration * 1000 + extension;
        }

        final Long httpExpiresTime = getHttpExpiresTime(pHttpExpiresHeader);
        if (httpExpiresTime != null) {
            return httpExpiresTime + extension;
        }

        return pNow + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + extension;
    }

    /**
     * Proxy for {@link SSLSocketFactory} that tries to enable TLSv1.2 and/or disable SSLv3 on
     * older devices to improve security and compatibility with modern https server configurations
     *
     * @since 6.1.7
     */
    private static class CompatibilitySocketFactory extends SSLSocketFactory {
        SSLSocketFactory sslSocketFactory;

        CompatibilitySocketFactory(SSLSocketFactory sslSocketFactory) {
            super();
            this.sslSocketFactory = sslSocketFactory;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return sslSocketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return sslSocketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket() throws IOException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
            return upgradeTlsAndRemoveSsl(socket);
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(s, host, port, autoClose);
            return upgradeTlsAndRemoveSsl(socket);
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            return upgradeTlsAndRemoveSsl(socket);
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
                UnknownHostException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port, localHost, localPort);
            return upgradeTlsAndRemoveSsl(socket);
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            return upgradeTlsAndRemoveSsl(socket);
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
                throws IOException {
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(address, port, localAddress, localPort);
            return upgradeTlsAndRemoveSsl(socket);
        }

        private SSLSocket upgradeTlsAndRemoveSsl(SSLSocket socket) {
            String[] supportedProtocols = socket.getSupportedProtocols();
            String[] enabledProtocols = socket.getEnabledProtocols();
            String[] newEnabledProtocols;

            // If TLS 1.2 is supported just set it as the only enabled protocol an be done with it,
            // as it's guaranteed to be the most modern protocol on devices on API<21 (1.3 only
            // exists since August 2018)
            if (Arrays.binarySearch(supportedProtocols, "TLSv1.2") >= 0) {
                newEnabledProtocols = new String[]{"TLSv1.2"};
            } else {
                int sslEnabled = Arrays.binarySearch(enabledProtocols, "SSLv3");
                if (sslEnabled >= 0) {
                    newEnabledProtocols = new String[enabledProtocols.length - 1];
                    System.arraycopy(enabledProtocols, 0, newEnabledProtocols, 0, sslEnabled);
                    if (newEnabledProtocols.length > sslEnabled) {
                        System.arraycopy(
                                enabledProtocols, sslEnabled + 1,
                                newEnabledProtocols, sslEnabled,
                                newEnabledProtocols.length - sslEnabled);
                    }
                } else {
                    newEnabledProtocols = enabledProtocols;
                }
            }

            socket.setEnabledProtocols(newEnabledProtocols);
            return socket;
        }
    }
}

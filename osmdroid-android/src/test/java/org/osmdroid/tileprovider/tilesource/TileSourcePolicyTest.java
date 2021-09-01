package org.osmdroid.tileprovider.tilesource;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import java.net.HttpURLConnection;
import java.util.Random;

/**
 * @author Fabrice Fontaine
 * Used to be TileDownloaderTest
 * @since 6.1.7
 */
public class TileSourcePolicyTest {

    private final long mCacheControlValue = 172800; // in seconds
    private final String[] mCacheControlStringOK = {"max-age=172800, public", "public, max-age=172800", "max-age=172800"};
    private final String[] mCacheControlStringKO = {"max-age=, public", "public"};

    private final long mExpiresValue = 1539971220000L;
    private final String[] mExpiresStringOK = {"Fri, 19 Oct 2018 17:47:00 GMT"};
    private final String[] mExpiresStringKO = {"Frfgi, 19 Oct 2018 17:47:00 GMT"};

    @Test
    public void testGetHttpExpiresTime() {
        final TileSourcePolicy tileSourcePolicy = new TileSourcePolicy();
        for (final String string : mExpiresStringOK) {
            Assert.assertEquals(mExpiresValue, (long) tileSourcePolicy.getHttpExpiresTime(string));
        }
        for (final String string : mExpiresStringKO) {
            Assert.assertNull(tileSourcePolicy.getHttpExpiresTime(string));
        }
    }

    @Test
    public void testGetHttpCacheControlDuration() {
        final TileSourcePolicy tileSourcePolicy = new TileSourcePolicy();
        for (final String string : mCacheControlStringOK) {
            Assert.assertEquals(mCacheControlValue, (long) tileSourcePolicy.getHttpCacheControlDuration(string));
        }
        for (final String string : mCacheControlStringKO) {
            Assert.assertNull(tileSourcePolicy.getHttpCacheControlDuration(string));
        }
    }

    @Test
    public void testComputeExpirationTime() {
        final Random random = new Random();
        final int oneWeek = 7 * 24 * 3600 * 1000; // 7 days in milliseconds
        testComputeExpirationTimeHelper(null, random.nextInt(oneWeek));
        testComputeExpirationTimeHelper((long) random.nextInt(oneWeek), random.nextInt(oneWeek));
    }

    private void testComputeExpirationTimeHelper(final Long pOverride, final long pExtension) {
        final TileSourcePolicy tileSourcePolicy = new TileSourcePolicy();
        final long now = System.currentTimeMillis();
        Configuration.getInstance().setExpirationOverrideDuration(pOverride);
        Configuration.getInstance().setExpirationExtendedDuration(pExtension);
        for (final String cacheControlString : mCacheControlStringOK) {
            final long expected = pOverride != null ? now + pOverride : now + mCacheControlValue * 1000 + pExtension;
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
        for (final String cacheControlString : mCacheControlStringKO) {
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : mExpiresValue + pExtension,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : now + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + pExtension,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
    }

    @Test
    public void testCustomExpirationTimeWithValues() {
        final long twentyMinutesInMillis = 20 * 60 * 1000;
        final TileSourcePolicy tileSourcePolicy = new TileSourcePolicy() {
            @Override
            public long computeExpirationTime(String pHttpExpiresHeader, String pHttpCacheControlHeader, long pNow) {
                return pNow + twentyMinutesInMillis;
            }
        };
        final long now = System.currentTimeMillis();
        final long expected = now + twentyMinutesInMillis;
        for (final String cacheControlString : mCacheControlStringOK) {
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
        for (final String cacheControlString : mCacheControlStringKO) {
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        expected,
                        tileSourcePolicy.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
    }

    @Test
    public void testCustomExpirationTimeWithHttpConnection() {
        final long twentyMinutesInMillis = 20 * 60 * 1000;
        final long thirtyMinutesInMillis = 30 * 60 * 1000;
        final HttpURLConnection dummyConnection = new HttpURLConnection(null) {
            @Override
            public void disconnect() {
            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() {
            }

            @Override
            public String getHeaderField(String name) {
                return null;
            }
        };
        final TileSourcePolicy tileSourcePolicy = new TileSourcePolicy() {
            @Override
            public long computeExpirationTime(String pHttpExpiresHeader, String pHttpCacheControlHeader, long pNow) {
                return pNow + twentyMinutesInMillis;
            }

            @Override
            public long computeExpirationTime(HttpURLConnection pHttpURLConnection, long pNow) {
                return pNow + thirtyMinutesInMillis;
            }
        };
        final long now = System.currentTimeMillis();
        final long expected = now + thirtyMinutesInMillis;
        Assert.assertEquals(
                expected,
                tileSourcePolicy.computeExpirationTime(dummyConnection, now));
    }
}
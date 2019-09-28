package org.osmdroid.tileprovider.modules;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import java.util.Random;

/**
 * @author Fabrice Fontaine
 * @since 6.0.3
 */
public class TileDownloaderTest {

    private final long mCacheControlValue           = 172800; // in seconds
    private final String mCacheControlStringOK[]    = {"max-age=172800, public", "public, max-age=172800", "max-age=172800"};
    private final String mCacheControlStringKO[]    = {"max-age=, public", "public"};

    private final long mExpiresValue                = 1539971220000L;
    private final String mExpiresStringOK[]         = {"Fri, 19 Oct 2018 17:47:00 GMT"};
    private final String mExpiresStringKO[]         = {"Frfgi, 19 Oct 2018 17:47:00 GMT"};

    @Test
    public void testGetHttpExpiresTime(){
        final TileDownloader tileDownloader = new TileDownloader();
        for (final String string : mExpiresStringOK) {
            Assert.assertEquals(mExpiresValue, (long) tileDownloader.getHttpExpiresTime(string));
        }
        for (final String string : mExpiresStringKO) {
            Assert.assertNull(tileDownloader.getHttpExpiresTime(string));
        }
    }

    @Test
    public void testGetHttpCacheControlDuration(){
        final TileDownloader tileDownloader = new TileDownloader();
        for (final String string : mCacheControlStringOK) {
            Assert.assertEquals(mCacheControlValue, (long) tileDownloader.getHttpCacheControlDuration(string));
        }
        for (final String string : mCacheControlStringKO) {
            Assert.assertNull(tileDownloader.getHttpCacheControlDuration(string));
        }
    }

    @Test
    public void testComputeExpirationTime(){
        final Random random = new Random();
        final int oneWeek = 7 * 24 * 3600 * 1000; // 7 days in milliseconds
        testComputeExpirationTimeHelper(null, random.nextInt(oneWeek));
        testComputeExpirationTimeHelper((long)random.nextInt(oneWeek), random.nextInt(oneWeek));
    }

    private void testComputeExpirationTimeHelper(final Long pOverride, final long pExtension){
        final TileDownloader tileDownloader = new TileDownloader();
        final long now = System.currentTimeMillis();
        Configuration.getInstance().setExpirationOverrideDuration(pOverride);
        Configuration.getInstance().setExpirationExtendedDuration(pExtension);
        for (final String cacheControlString : mCacheControlStringOK) {
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : now + mCacheControlValue * 1000 + pExtension,
                        tileDownloader.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : now + mCacheControlValue * 1000 + pExtension,
                        tileDownloader.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
        for (final String cacheControlString : mCacheControlStringKO) {
            for (final String expiresString : mExpiresStringOK) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : mExpiresValue + pExtension,
                        tileDownloader.computeExpirationTime(expiresString, cacheControlString, now));
            }
            for (final String expiresString : mExpiresStringKO) {
                Assert.assertEquals(
                        pOverride != null ? now + pOverride : now + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + pExtension,
                        tileDownloader.computeExpirationTime(expiresString, cacheControlString, now));
            }
        }
    }
}

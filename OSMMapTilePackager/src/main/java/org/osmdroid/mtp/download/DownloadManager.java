// Created by plusminus on 9:34:16 PM - Mar 5, 2009
package org.osmdroid.mtp.download;

import org.osmdroid.mtp.OSMMapTilePackager;
import org.osmdroid.mtp.TileWriter;
import org.osmdroid.mtp.adt.OSMTileInfo;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    private static final Logger LOG = LoggerFactory.getLogger(DownloadManager.class);

    private final ExecutorService mThreadPool;

    private final Queue<OSMTileInfo> mQueue = new LinkedBlockingQueue<OSMTileInfo>();

    private final String mBaseURL;
    private TileWriter writer;
    private String sourceName;
    private OSMMapTilePackager.TileJob job;
    // ===========================================================
    // Constructors
    // ===========================================================

    public DownloadManager(String sourceName, final String pBaseURL, final int mThreads, TileWriter writer, OSMMapTilePackager.TileJob job) {
        this.sourceName = sourceName;
        this.mBaseURL = pBaseURL;
        this.writer = writer;
        this.mThreadPool = Executors.newFixedThreadPool(mThreads);
        this.job = job;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public synchronized void add(final OSMTileInfo pTileInfo) {
        this.mQueue.add(pTileInfo);
        spawnNewThread();
    }

    public void cancel() {
        mThreadPool.shutdownNow();
        mQueue.clear();
    }

    private synchronized OSMTileInfo getNext() {
        final OSMTileInfo tile = this.mQueue.poll();

        final int remaining = this.mQueue.size();
        if (remaining % 10 == 0 && remaining > 0) {
            System.out.print("(" + remaining + ")");
        } else {
            System.out.print(".");
        }

        this.notify();
        return tile;
    }

    public synchronized void waitEmpty() throws InterruptedException {
        while (this.mQueue.size() > 0) {
            this.wait();
        }
    }

    public void waitFinished() throws InterruptedException {
        waitEmpty();
        this.mThreadPool.shutdown();
        this.mThreadPool.awaitTermination(6, TimeUnit.HOURS);
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    private void spawnNewThread() {
        this.mThreadPool.execute(new DownloadRunner());
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


    private class DownloadRunner implements Runnable {

        private OSMTileInfo mTileInfo;

        public DownloadRunner() {
        }

        private void init(final OSMTileInfo pTileInfo) {
            this.mTileInfo = pTileInfo;
            /* Create destination file. */
        }

        @Override
        public void run() {
            InputStream in = null;
            ByteArrayOutputStream baos = null;
            init(DownloadManager.this.getNext());
            //this assumes z/x/y, but there's definitely z/y/x servers out there
            final String finalURL = DownloadManager.this.mBaseURL.
                    replace("{z}", this.mTileInfo.zoom + "").
                    replace("{x}", this.mTileInfo.x + "").
                    replace("{y}", this.mTileInfo.y + "");


            try {
                URL url = new URL(finalURL);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Osmdroid Tile Packager ");
                in = new BufferedInputStream(urlConnection.getInputStream(), StreamUtils.IO_BUFFER_SIZE);
                baos = new ByteArrayOutputStream();
                StreamUtils.copy(in, baos);

                baos.flush();
                in.close();
                baos.close();
                job.incProgress();
                writer.write(this.mTileInfo.zoom, this.mTileInfo.x, this.mTileInfo.y, sourceName, baos.toByteArray());
            } catch (final Exception e) {
                LOG.warn("Error downloading: '" + this.mTileInfo + "' from URL: " + finalURL + " : " + e);
                job.incFailed();
                DownloadManager.this.add(this.mTileInfo); // try again later
            } finally {

                StreamUtils.closeStream(in);
                StreamUtils.closeStream(baos);
            }
        }
    }
}

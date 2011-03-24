// Created by plusminus on 9:34:16 PM - Mar 5, 2009
package org.andnav2.osm.mtp.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.andnav2.osm.mtp.adt.OSMTileInfo;
import org.osmdroid.tileprovider.util.StreamUtils;

public class DownloadManager {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final ExecutorService mThreadPool;

	private final Queue<OSMTileInfo> mQueue = new LinkedBlockingQueue<OSMTileInfo>();

	private final String mBaseURL;
	private final String mDestinationURL;

	// ===========================================================
	// Constructors
	// ===========================================================

	public DownloadManager(final String pBaseURL, final String pDestinationURL, final int mThreads) {
		this.mBaseURL = pBaseURL;
		this.mDestinationURL = pDestinationURL;
		this.mThreadPool = Executors.newFixedThreadPool(mThreads);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public synchronized void add(final OSMTileInfo pTileInfo){
		this.mQueue.add(pTileInfo);
		spawnNewThread();
	}

	private synchronized OSMTileInfo getNext(){
		final OSMTileInfo tile = this.mQueue.poll();

		final int remaining = this.mQueue.size();
		if(remaining % 10 == 0 && remaining > 0) {
			System.out.print("(" + remaining +")");
		} else {
			System.out.print(".");
		}

		this.notify();
		return tile;
	}

	public synchronized void waitEmpty() throws InterruptedException {
		while(this.mQueue.size() > 0){
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
		private File mDestinationFile;

		public DownloadRunner() {
		}

		private void init(final OSMTileInfo pTileInfo) {
			this.mTileInfo = pTileInfo;
			/* Create destination file. */
			final String filename = String.format(DownloadManager.this.mDestinationURL, this.mTileInfo.zoom, this.mTileInfo.x, this.mTileInfo.y);
			this.mDestinationFile = new File(filename);

			final File parent = this.mDestinationFile.getParentFile();
			parent.mkdirs();
		}

		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;

			init(DownloadManager.this.getNext());

			if (mDestinationFile.exists()) {
				return; // TODO issue 70 - make this an option
			}

			final String finalURL = String.format(DownloadManager.this.mBaseURL, this.mTileInfo.zoom, this.mTileInfo.x, this.mTileInfo.y);

			try {
				in = new BufferedInputStream(new URL(finalURL).openStream(), StreamUtils.IO_BUFFER_SIZE);

				final FileOutputStream fileOut = new FileOutputStream(this.mDestinationFile);
				out = new BufferedOutputStream(fileOut, StreamUtils.IO_BUFFER_SIZE);

				StreamUtils.copy(in, out);

				out.flush();
			} catch (final Exception e) {
				System.err.println("Error downloading: '" + this.mTileInfo + "' from URL: " + finalURL + " : " + e);
				DownloadManager.this.add(this.mTileInfo); // try again later
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
			}
		}
	}
}

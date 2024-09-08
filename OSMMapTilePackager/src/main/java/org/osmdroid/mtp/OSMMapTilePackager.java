// Created by plusminus on 9:22:20 PM - Mar 5, 2009
package org.osmdroid.mtp;

import org.osmdroid.mtp.adt.OSMTileInfo;
import org.osmdroid.mtp.download.DownloadManager;
import org.osmdroid.mtp.ui.OSMMapTilePackagerUI;
import org.osmdroid.mtp.util.DbCreator;
import org.osmdroid.mtp.util.FolderZipper;
import org.osmdroid.mtp.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class OSMMapTilePackager {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final Logger LOG = LoggerFactory.getLogger(OSMMapTilePackager.class);
    private static final int THREADCOUNT_DEFAULT = 2;
    private static boolean FORCE = false;

    // ===========================================================
    // Fields
    // ===========================================================
    // ===========================================================
    // Constructors
    // ===========================================================
    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            printUsageAndExit();
        }

        /* Parsing will only start if this variable was set. */
        FORCE = false;
        String name = null;
        String serverURL = null;
        String destinationFile = null;
        String tempFolder = null;
        String fileAppendix = "";
        Double north = null;
        Double south = null;
        Double east = null;
        Double west = null;
        Integer maxzoom = null;
        int minzoom = 0;
        int threadCount = THREADCOUNT_DEFAULT;

        try {
            for (int i = 0; i < args.length; i += 2) {
                if (args[i].equals("-u")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        serverURL = args[i + 1];
                    }
                } else if (args[i].equals("-force")) {
                    i--;
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        FORCE = true;
                    }
                } else if (args[i].equals("-n")) {
                    i--;
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        name = args[i + 1];
                    }
                } else if (args[i].equals("-d")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        destinationFile = args[i + 1];
                    }
                } else if (args[i].equals("-fa")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        fileAppendix = args[i + 1];
                    }
                } else if (args[i].equals("-nthreads")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        threadCount = Integer.parseInt(args[i + 1]);
                    }
                } else if (args[i].equals("-zmin")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        minzoom = Integer.parseInt(args[i + 1]);
                    }
                } else if (args[i].equals("-zmax")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        maxzoom = Integer.parseInt(args[i + 1]);
                    }
                } else if (args[i].equals("-t")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        tempFolder = args[i + 1];
                    }
                } else if (args[i].equals("-n")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        north = Double.parseDouble(args[i + 1]);
                    }
                } else if (args[i].equals("-s")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        south = Double.parseDouble(args[i + 1]);
                    }
                } else if (args[i].equals("-e")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        east = Double.parseDouble(args[i + 1]);
                    }
                } else if (args[i].equals("-w")) {
                    if (i >= args.length) {
                        printUsageAndExit();
                    } else {
                        west = Double.parseDouble(args[i + 1]);
                    }
                } else if (args[i].equals("-gui")) {
                    OSMMapTilePackagerUI.main(null);
                    return;
                }
            }
        } catch (final NumberFormatException nfe) {
            printUsageAndExit();
        }

        if (tempFolder == null) {
            printUsageAndExit();
        }

        if (serverURL == null && !new File(tempFolder).exists()) {
            printUsageAndExit();
        }

        if (north == null || south == null || east == null || west == null) {
            printUsageAndExit();
        }

        OSMMapTilePackager exec = new OSMMapTilePackager();
        TileJob runner = new TileJob();
        runner.tileSourceName = name;
        runner.serverURL = serverURL;
        runner.destinationFile = destinationFile;
        runner.threadCount = threadCount;
        runner.bounds.add(new Bounds(maxzoom, minzoom, north, south, east, west));

        runner.total = runFileExpecterWithAbort(runner);
        exec.job = runner;
        exec.execute();
    }

    public TileJob getJob() {
        return job;
    }

    public void setJob(TileJob job) {
        this.job = job;
    }

    private TileJob job;

    public static class TileJob {
        public String getDestinationFile() {
            return destinationFile;
        }

        public void setDestinationFile(String destinationFile) {
            this.destinationFile = destinationFile;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getProgress() {
            return progress.get();
        }

        public void setProgress(int progress) {
            this.progress = new AtomicInteger(progress);
        }

        public String getServerURL() {
            return serverURL;
        }

        public void setServerURL(String serverURL) {
            this.serverURL = serverURL;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }

        public String getTileSourceName() {
            return tileSourceName;
        }

        public void setTileSourceName(String tileSourceName) {
            this.tileSourceName = tileSourceName;
        }

        public List<Bounds> getBounds() {
            return bounds;
        }

        public void setBounds(List<Bounds> bounds) {
            this.bounds = bounds;
        }

        private String destinationFile;

        private int total = 0;

        public int getTilesFailed() {
            return tilesFailed.get();
        }

        public void incFailed() {
            this.tilesFailed.incrementAndGet();
        }

        public void setTilesFailed(int tilesFailed) {
            this.tilesFailed = new AtomicInteger(tilesFailed);
        }

        private AtomicInteger tilesFailed = new AtomicInteger(0);
        private AtomicInteger progress = new AtomicInteger(0);
        private String serverURL;
        private int threadCount;

        private String tileSourceName;
        private List<Bounds> bounds = new ArrayList<>();

        public void incProgress() {
            progress.incrementAndGet();
        }
    }

    public static class Bounds {
        public Bounds() {
        }

        public Bounds(int minZoom, int maxZoom, double north, double south, double east, double west) {
            this.minZoom = minZoom;
            this.maxZoom = maxZoom;
            this.north = north;
            this.south = south;
            this.east = east;
            this.west = west;
        }

        public int getMinZoom() {
            return minZoom;
        }

        public void setMinZoom(int minZoom) {
            this.minZoom = minZoom;
        }

        public int getMaxZoom() {
            return maxZoom;
        }

        public void setMaxZoom(int maxZoom) {
            this.maxZoom = maxZoom;
        }

        public double getNorth() {
            return north;
        }

        public void setNorth(double north) {
            this.north = north;
        }

        public double getSouth() {
            return south;
        }

        public void setSouth(double south) {
            this.south = south;
        }

        public double getEast() {
            return east;
        }

        public void setEast(double east) {
            this.east = east;
        }

        public double getWest() {
            return west;
        }

        public void setWest(double west) {
            this.west = west;
        }

        int minZoom;
        int maxZoom;
        double north;
        double south;
        double east;
        double west;
    }


    public ProgressNotification getCallbackNotification() {
        return callbackNotification;
    }

    public void setCallbackNotification(ProgressNotification callbackNotification) {
        this.callbackNotification = callbackNotification;
    }

    private ProgressNotification callbackNotification;

    /**
     * this starts executing the download and packaging
     */
    public void run() throws Exception {

        File parentFile = new File(job.destinationFile).getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                LOG.error("Failed to mkdirs at " + parentFile.getAbsolutePath());
            }
        }
        job.total = runFileExpecter(job);
        execute();


    }

    public void cancel() {
        dm.cancel();
    }

    public interface ProgressNotification {

        void updateProgress(String msg,
                            int perfecentComplete,
                            int tilesProcessed,
                            int tilesFailed);
    }


    public void execute() throws Exception {
        LOG.info("Starting job");
        TileWriter writer = null;
        if (job.destinationFile.endsWith(".zip")) {
            writer = new FolderZipper(new File(job.destinationFile));
        } else {
            writer = new DbCreator(new File(job.destinationFile));
        }
        writer.open();
        if (callbackNotification != null) {
            callbackNotification.updateProgress("Started", 0,
                    job.getProgress(), job.getTilesFailed());
        }
        for (Bounds b : job.bounds) {
            runDownloading(job.tileSourceName, job.serverURL, job.threadCount, b.minZoom, b.maxZoom, b.north, b.south, b.east, b.west, callbackNotification, writer);
        }
        writer.close();
        if (callbackNotification != null) {
            callbackNotification.updateProgress("Download complete, creating archive",
                    100, job.getProgress(), job.getTilesFailed());
        }
    }


    private static void printUsageAndExit() {
        LOG.info("Usage:\n"
                + "-gui\tLaunch the GUI\n"
                + "-u\t[OSM-style tile URL: http://_URL_/%d/%d/%d.png]\n"
                + "-t\t[Temporary Folder]\n"
                + "-d\t[Destination-file: C:\\mappack.zip]\n"
                + "-zmin\t[Minimum zoomLevel to download. Default: 0]\n"
                + "-zmax\t[Maximum zoomLevel to download]\n"
                + "-fa\t[Filename-Appendix. Default: \"\"]\n"
                + "-n\t[North Latitude]\n"
                + "-s\t[South Latitude]\n"
                + "-e\t[East Longitude]\n"
                + "-w\t[West Longitude]\n"
                + "-n\t[Tile source name]\n"
                + "-nthreads\t[Number of Download-Threads. Default: 2]\n"
        );
        System.exit(0);
    }


    DownloadManager dm;

    private void runDownloading(final String pName, final String pBaseURL, final int pThreadCount,
                                final int pMinZoom, final int pMaxZoom, final double pNorth,
                                final double pSouth, final double pEast, final double pWest, final ProgressNotification callbackNotification,
                                TileWriter writer) {

        dm = new DownloadManager(pName, pBaseURL, pThreadCount, writer, job);

        /* For each zoomLevel. */
        for (int z = pMinZoom; z <= pMaxZoom; z++) {
            final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(pNorth, pWest, z);
            final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(pSouth, pEast, z);

            LOG.debug("ZoomLevel: " + z + " ");

            for (int x = upperLeft.x; x <= lowerRight.x; x++) {
                for (int y = upperLeft.y; y <= lowerRight.y; y++) {
                    if (callbackNotification != null) {
                        callbackNotification.updateProgress("Downloading " + z + "/" + x + "/" + y,
                                job.total > 0 ? (int) ((double) ((double) job.getProgress() / (double) job.total)) * 100 : 0,
                                job.getProgress(), job.getTilesFailed());
                    }
                    dm.add(new OSMTileInfo(x, y, z));
                }
            }
            try {
                dm.waitEmpty();
                LOG.info(" done.");
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            LOG.info("Awaiting termination of all threads ...");
            dm.waitFinished();
            LOG.info(" done.");
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int runFileExpecter(TileJob job) {
        /* Calculate file-count. */
        int fileCnt = 0;
        for (Bounds b : job.bounds) {
            for (int z = b.minZoom; z <= b.maxZoom; z++) {
                final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(b.north, b.west, z);
                final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(b.south, b.east, z);

                final int dx = lowerRight.x - upperLeft.x + 1;
                final int dy = lowerRight.y - upperLeft.y + 1;
                fileCnt += dx * dy;
            }
        }
        return fileCnt;
    }

    private static int runFileExpecterWithAbort(TileJob job) {
        int cnt = 0;
        for (Bounds b : job.bounds) {
            cnt = runFileExpecter(job);
        }
        abortIfUserIsNotSure("This will download: " + cnt + " Maptiles!");
        return cnt;

    }

    private static void abortIfUserIsNotSure(final String message) {
        if (FORCE) {
            return;
        }

        LOG.info(message);
        System.out.print("Are you sure? [Y/N] ?: ");
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (final Throwable t) {
            /* Ignore */
        }

        final String line = new Scanner(System.in).nextLine().toUpperCase().trim();

        if (!line.equals("Y") && !line.equals("YES")) {
            System.err.println("User aborted.");
            System.exit(0);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

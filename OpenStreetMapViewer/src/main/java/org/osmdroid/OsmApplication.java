package org.osmdroid;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is the base application for the sample app. We only use to catch errors during development cycles
 * <p>
 * Also see note on setting the UserAgent value
 * Created by alex on 7/4/16.
 */

@ReportsCrashes(formUri = "")
public class OsmApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        Thread.currentThread().setUncaughtExceptionHandler(new OsmUncaughtExceptionHandler());

        //https://github.com/osmdroid/osmdroid/issues/366

        //super important. Many tile servers, including open street maps, will BAN applications by user
        //agent. Do not use the sample application's user agent for your app! Use your own setting, such
        //as the app id.
        Configuration.getInstance().setUserAgentValue(getPackageName());

        /*
        FIXME, need a key for bing
        BingMapTileSource.retrieveBingKey(this);
        final BingMapTileSource source = new BingMapTileSource(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                source.initMetaData();
            }
        }).start();
        source.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        TileSourceFactory.addTileSource(source);

        final BingMapTileSource source2 = new BingMapTileSource(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                source2.initMetaData();
            }
        }).start();
        source2.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
        TileSourceFactory.addTileSource(source2);
        */


        //FIXME need a key for this TileSourceFactory.addTileSource(TileSourceFactory.CLOUDMADESMALLTILES);

        //FIXME need a key for this TileSourceFactory.addTileSource(TileSourceFactory.CLOUDMADESTANDARDTILES);


        //the sample app a few additional tile sources that we have api keys for, so add them here
        //this will automatically show up in the tile source list
        //FIXME this key is expired TileSourceFactory.addTileSource(new HEREWeGoTileSource(getApplicationContext()));
        //TileSourceFactory.addTileSource(new MapBoxTileSource(getApplicationContext()));
        //TileSourceFactory.addTileSource(new MapQuestTileSource(getApplicationContext()));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        try {
            // Initialise ACRA
            ACRA.init(this);
            ACRA.getErrorReporter().setReportSender(new ErrorFileWriter());
        } catch (Throwable t) {
            t.printStackTrace();
            //this can happen on androidx86 getExternalStorageDir is not writable or if there is a
            //permissions issue
        }
    }

    public static class OsmUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("UncaughtException", "Got an uncaught exception: " + ex.toString());
            if (ex.getClass().equals(OutOfMemoryError.class)) {
                writeHprof();
            }
            ex.printStackTrace();
        }
    }

    /**
     * writes the current heap to the file system at /sdcard/osmdroid/trace-{timestamp}.hprof
     * again, used only during out CI/memory leak tests
     */
    public static void writeHprof() {
        try {
            android.os.Debug.dumpHprofData(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/trace-" + System.currentTimeMillis() + ".hprof");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Writes hard crash stack traces to a file on the SD card.
     */
    private static class ErrorFileWriter implements ReportSender {

        @Override
        public void send(Context context, CrashReportData crashReportData) throws ReportSenderException {
            try {
                String rootDirectory = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();
                File f = new File(rootDirectory
                        + File.separatorChar
                        + "osmdroid"
                        + File.separatorChar);
                f.mkdirs();
                f = new File(rootDirectory
                        + File.separatorChar
                        + "osmdroid"
                        + File.separatorChar
                        + "crash.log");
                if (f.exists())
                    f.delete();


                f.createNewFile();
                PrintWriter pw = new PrintWriter(new FileWriter(f));
                pw.println(crashReportData.toString());
                pw.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }


        }
    }
}

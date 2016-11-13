package org.osmdroid;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is the base application for the sample app. We only use to catch errors during development cycles
 * Created by alex on 7/4/16.
 */
@ReportsCrashes( formUri = "")
public class OsmApplication extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        try {
            LeakCanary.install(this);
        }catch (Throwable ex){

            //this can happen on androidx86 getExternalStorageDir is not writable
            ex.printStackTrace();
        }
        Thread.currentThread().setUncaughtExceptionHandler(new OsmUncaughtExceptionHandler());

        //https://github.com/osmdroid/osmdroid/issues/366

        //super important. Many tile servers, including open street maps, will BAN applications by user
        //agent. Do not use the sample application's user agent for your app! Use your own setting, such
        //as the app id.
        OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);


        try {
            // Initialise ACRA
            ACRA.init(this);
            ACRA.getErrorReporter().setReportSender(new ErrorFileWriter());
        }catch (Throwable t){
            t.printStackTrace();
            //this can happen on androidx86 getExternalStorageDir is not writable
        }


    }

    public static class OsmUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("UncaughtException", "Got an uncaught exception: "+ex.toString());
            if(ex.getClass().equals(OutOfMemoryError.class))
            {
                writeHprof();
            }
            ex.printStackTrace();
        }
    }

    /**
     * writes the current heap to the file system at /sdcard/osmdroid/trace-{timestamp}.hprof
     */
    public static void writeHprof(){
        try {
            android.os.Debug.dumpHprofData(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/trace-" + System.currentTimeMillis()+ ".hprof");
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

            try {
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

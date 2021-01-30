// Created by plusminus on 18:23:13 - 03.10.2008
package org.osmdroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.debug.CacheAnalyzerActivity;
import org.osmdroid.diag.DiagnosticsActivity;
import org.osmdroid.intro.IntroActivity;
import org.osmdroid.samples.SampleWithMinimapItemizedoverlay;
import org.osmdroid.samples.SampleWithTilesOverlay;
import org.osmdroid.samples.SampleWithTilesOverlayAndCustomTileSource;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "OSM";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Generate a ListView with Sample Maps
        final ArrayList<String> list = new ArrayList<>();
        list.add("OSMDroid Sample Map (Start Here)");
        list.add("Sample with ItemizedOverlay");
        list.add("Sample with TilesOverlay");
        list.add("Sample with TilesOverlay and custom TileSource");
        list.add("More Samples");

        list.add("Report a Bug");
        list.add("Settings");
        list.add("Bug Drivers");
        list.add("Diagnostics");
        list.add("View the Intro again");
        list.add("Licenses");
        list.add("Cache Analyzer");

        ListView lv = findViewById(R.id.activitylist);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                this.startActivity(new Intent(this, StarterMapActivity.class));
                break;
            case 1:
                this.startActivity(new Intent(this, SampleWithMinimapItemizedoverlay.class));
                break;
            case 2:
                this.startActivity(new Intent(this, SampleWithTilesOverlay.class));
                break;
            case 3:
                this.startActivity(new Intent(this, SampleWithTilesOverlayAndCustomTileSource.class));
                break;
            case 4:
                this.startActivity(new Intent(this, ExtraSamplesActivity.class));
                break;
            case 5:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/osmdroid/osmdroid/issues/new"));
                startActivity(browserIntent);
                break;
            case 6: {
                Intent i = new Intent(this, PreferenceActivity.class);
                startActivity(i);
            }
            break;
            case 7:
                this.startActivity(new Intent(this, BugsTestingActivity.class));
                break;
            case 8:
                this.startActivity(new Intent(this, DiagnosticsActivity.class));
                break;
            case 9: {
                //skip this nonsense
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                edit.remove("osmdroid_first_ran");
                edit.commit();

                Intent intent = new Intent(this, IntroActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 10: {
                Intent i = new Intent(this, LicenseActivity.class);
                startActivity(i);
                break;
            }
            case 11:
                Intent starter = new Intent(this, CacheAnalyzerActivity.class);
                startActivity(starter);
                break;
        }
    }

    public void onResume() {
        super.onResume();
        updateStorageInfo();
        checkForCrashLogs();
    }

    private void checkForCrashLogs() {
        //look for osmdroid crash logs
        File root = Environment.getExternalStorageDirectory();
        String pathToMyAttachedFile = "/osmdroid/crash.log";
        final File file = new File(root, pathToMyAttachedFile);
        if (!file.exists() || !file.canRead()) {
            return;
        }

        //if found, prompt user to send to
        //osmdroidbugs@gmail.com

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"osmdroidbugs@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Open Map crash log");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Log data");

                        Uri uri = Uri.fromFile(file);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        file.delete();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crash logs");
        builder.setMessage("Sorry, it looks like we crashed at some point, would you mind sending us the" +
                "crash log?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /**
     * refreshes the current osmdroid cache paths with user preferences plus soe logic to work around
     * file system permissions on api23 devices. it's primarily used for out android tests.
     *
     * @param ctx
     * @return current cache size in bytes
     */
    public static long updateStoragePreferences(Context ctx) {

        //loads the osmdroid config from the shared preferences object.
        //if this is the first time launching this app, all settings are set defaults with one exception,
        //the tile cache. the default is the largest write storage partition, which could end up being
        //this app's private storage, depending on device config and permissions

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //also note that our preference activity has the corresponding save method on the config object, but it can be called at any time.


        File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
        if (dbFile.exists()) {
            return dbFile.length();
        }
        return -1;
    }

    /**
     * gets storage state and current cache size
     */
    private void updateStorageInfo() {

        long cacheSize = updateStoragePreferences(this);
        //cache management ends here

        TextView tv = findViewById(R.id.sdcardstate_value);
        final String state = Environment.getExternalStorageState();

        boolean mSdCardAvailable = Environment.MEDIA_MOUNTED.equals(state);
        tv.setText((mSdCardAvailable ? "Mounted" : "Not Available"));
        if (!mSdCardAvailable) {
            tv.setTextColor(Color.RED);
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv = findViewById(R.id.version_text);
        tv.setText(BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE);

        tv = findViewById(R.id.mainstorageInfo);
        tv.setText(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + "\n" +
                "Cache size: " + Formatter.formatFileSize(this, cacheSize));
    }
}

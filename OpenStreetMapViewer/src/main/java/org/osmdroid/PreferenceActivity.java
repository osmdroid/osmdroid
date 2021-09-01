package org.osmdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.DefaultConfigurationProvider;
import org.osmdroid.intro.StorageAdapter;
import org.osmdroid.model.PositiveLongTextValidator;
import org.osmdroid.model.PositiveShortTextValidator;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * OK so why is here?
 * Stupid reason #1: Android Studio's wizard generates a bunch of stupid complex code
 * Stupid reason #2: Android's Preference Activity is API10+ and we (osmdroid) are API8+
 * Stupid reason #3: Simple is better, usually
 *
 * @since 5.6
 * Created by alex on 10/21/16.
 */

public class PreferenceActivity extends AppCompatActivity implements View.OnClickListener {
    CheckBox checkBoxDebugTileProvider,
            checkBoxDebugMode,
            checkBoxHardwareAcceleration,
            checkBoxMapViewDebug,
            checkBoxDebugDownloading;
    Button buttonSetCache,
            buttonManualCacheEntry,
            buttonPurgeCache,
            buttonReset,
            buttonSetBase,
            buttonManualBaseEntry;
    TextView textViewCacheDirectory;
    TextView textViewBaseDirectory;
    EditText httpUserAgent,
            tileDownloadThreads,
            tileDownloadMaxQueueSize,
            cacheMapTileCount,
            cacheMaxSize,
            cacheTrimSize,
            tileFileSystemThreads,
            tileFileSystemMaxQueueSize,
            gpsWaitTime,
            additionalExpirationTime,
            overrideExpirationTime,
            zoomSpeedDefault,
            zoomSpeedShort;
    boolean abortSave = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        checkBoxDebugTileProvider = findViewById(R.id.checkBoxDebugTileProvider);
        checkBoxDebugMode = findViewById(R.id.checkBoxDebugMode);
        checkBoxHardwareAcceleration = findViewById(R.id.checkBoxHardwareAcceleration);
        checkBoxDebugDownloading = findViewById(R.id.checkBoxDebugDownloading);
        checkBoxMapViewDebug = findViewById(R.id.checkBoxMapViewDebug);
        checkBoxDebugTileProvider.setOnClickListener(this);
        checkBoxDebugMode.setOnClickListener(this);
        checkBoxHardwareAcceleration.setOnClickListener(this);
        checkBoxMapViewDebug.setOnClickListener(this);

        textViewCacheDirectory = findViewById(R.id.textViewCacheDirectory);
        textViewBaseDirectory = findViewById(R.id.textViewBaseDirectory);
        buttonPurgeCache = findViewById(R.id.buttonPurgeCache);
        httpUserAgent = findViewById(R.id.httpUserAgent);
        tileDownloadThreads = findViewById(R.id.tileDownloadThreads);
        tileDownloadThreads.addTextChangedListener(new PositiveShortTextValidator(tileDownloadThreads));
        tileDownloadMaxQueueSize = findViewById(R.id.tileDownloadMaxQueueSize);
        tileDownloadMaxQueueSize.addTextChangedListener(new PositiveShortTextValidator(tileDownloadMaxQueueSize));
        cacheMapTileCount = findViewById(R.id.cacheMapTileCount);
        cacheMapTileCount.addTextChangedListener(new PositiveShortTextValidator(cacheMapTileCount));
        tileFileSystemThreads = findViewById(R.id.tileFileSystemThreads);
        tileFileSystemThreads.addTextChangedListener(new PositiveShortTextValidator(tileFileSystemThreads));
        tileFileSystemMaxQueueSize = findViewById(R.id.tileFileSystemMaxQueueSize);
        tileFileSystemMaxQueueSize.addTextChangedListener(new PositiveShortTextValidator(tileFileSystemMaxQueueSize));
        gpsWaitTime = findViewById(R.id.gpsWaitTime);
        gpsWaitTime.addTextChangedListener(new PositiveLongTextValidator(gpsWaitTime, 1));
        additionalExpirationTime = findViewById(R.id.additionalExpirationTime);
        additionalExpirationTime.addTextChangedListener(new PositiveLongTextValidator(additionalExpirationTime, 0));

        cacheMaxSize = findViewById(R.id.cacheMaxSize);
        cacheTrimSize = findViewById(R.id.cacheTrimSize);
        cacheMaxSize.addTextChangedListener(new PositiveLongTextValidator(cacheMaxSize, 0));
        cacheTrimSize.addTextChangedListener(new PositiveLongTextValidator(cacheTrimSize, 0));

        overrideExpirationTime = findViewById(R.id.overrideExpirationTime);
        zoomSpeedDefault = findViewById(R.id.zoomSpeedDefault);
        zoomSpeedDefault.addTextChangedListener(new PositiveLongTextValidator(zoomSpeedDefault, 1));
        zoomSpeedShort = findViewById(R.id.zoomSpeedShort);
        zoomSpeedShort.addTextChangedListener(new PositiveLongTextValidator(zoomSpeedShort, 1));


        buttonSetBase = findViewById(R.id.buttonSetBase);
        buttonSetBase.setOnClickListener(this);
        buttonSetCache = findViewById(R.id.buttonSetCache);
        buttonManualCacheEntry = findViewById(R.id.buttonManualCacheEntry);
        buttonSetCache.setOnClickListener(this);
        buttonManualBaseEntry = findViewById(R.id.buttonManualBaseEntry);
        buttonManualBaseEntry.setOnClickListener(this);
        buttonManualCacheEntry.setOnClickListener(this);
        buttonPurgeCache.setOnClickListener(this);
        buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(this);

        findViewById(R.id.baseDirTitle).setOnClickListener(this);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        tileFileSystemMaxQueueSize.setText(Configuration.getInstance().getTileFileSystemMaxQueueSize() + "");
        tileFileSystemThreads.setText(Configuration.getInstance().getTileFileSystemThreads() + "");
        tileDownloadMaxQueueSize.setText(Configuration.getInstance().getTileDownloadMaxQueueSize() + "");
        tileDownloadThreads.setText(Configuration.getInstance().getTileDownloadThreads() + "");
        gpsWaitTime.setText(Configuration.getInstance().getGpsWaitTime() + "");
        additionalExpirationTime.setText(Configuration.getInstance().getExpirationExtendedDuration() + "");
        cacheMapTileCount.setText(Configuration.getInstance().getCacheMapTileCount() + "");
        if (Configuration.getInstance().getExpirationOverrideDuration() != null)
            overrideExpirationTime.setText(Configuration.getInstance().getExpirationOverrideDuration() + "");

        httpUserAgent.setText(Configuration.getInstance().getUserAgentValue());
        checkBoxMapViewDebug.setChecked(Configuration.getInstance().isDebugMapView());
        checkBoxDebugMode.setChecked(Configuration.getInstance().isDebugMode());
        checkBoxDebugTileProvider.setChecked(Configuration.getInstance().isDebugTileProviders());
        checkBoxHardwareAcceleration.setChecked(Configuration.getInstance().isMapViewHardwareAccelerated());
        checkBoxDebugDownloading.setChecked(Configuration.getInstance().isDebugMapTileDownloader());
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath());
        textViewBaseDirectory.setText(Configuration.getInstance().getOsmdroidBasePath().getAbsolutePath());

        cacheMaxSize.setText(Configuration.getInstance().getTileFileSystemCacheMaxBytes() + "");
        cacheTrimSize.setText(Configuration.getInstance().getTileFileSystemCacheTrimBytes() + "");

        zoomSpeedDefault.setText(Configuration.getInstance().getAnimationSpeedDefault() + "");
        zoomSpeedShort.setText(Configuration.getInstance().getAnimationSpeedShort() + "");

    }

    @Override
    public void onPause() {
        super.onPause();
        if (abortSave)
            return;
        //save the configuration
        try {
            if (tileDownloadThreads.getError() == null)
                Configuration.getInstance().setTileDownloadThreads(Short.parseShort(tileDownloadThreads.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (tileDownloadMaxQueueSize.getError() == null)
                Configuration.getInstance().setTileDownloadMaxQueueSize(Short.parseShort(tileDownloadMaxQueueSize.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (cacheMapTileCount.getError() == null)
                Configuration.getInstance().setCacheMapTileCount(Short.parseShort(cacheMapTileCount.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (tileFileSystemThreads.getError() == null)
                Configuration.getInstance().setTileFileSystemThreads(Short.parseShort(tileFileSystemThreads.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (tileFileSystemMaxQueueSize.getError() == null)
                Configuration.getInstance().setTileFileSystemMaxQueueSize(Short.parseShort(tileFileSystemMaxQueueSize.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (gpsWaitTime.getError() == null)
                Configuration.getInstance().setGpsWaitTime(Long.parseLong(gpsWaitTime.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (additionalExpirationTime.getError() == null)
                Configuration.getInstance().setExpirationExtendedDuration(Long.parseLong(additionalExpirationTime.getText().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            Long val = Long.parseLong(overrideExpirationTime.getText().toString());
            if (val > 0)
                Configuration.getInstance().setExpirationOverrideDuration(val);
            else
                Configuration.getInstance().setExpirationOverrideDuration(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Configuration.getInstance().setExpirationOverrideDuration(null);
        }

        try {
            Long val = Long.parseLong(cacheMaxSize.getText().toString());
            if (val > 0)
                Configuration.getInstance().setTileFileSystemCacheMaxBytes(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Long val = Long.parseLong(cacheTrimSize.getText().toString());
            if (val > 0)
                Configuration.getInstance().setTileFileSystemCacheTrimBytes(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Configuration.getInstance().setUserAgentValue(httpUserAgent.getText().toString());
        Configuration.getInstance().setDebugMapView(checkBoxMapViewDebug.isChecked());
        Configuration.getInstance().setDebugMode(checkBoxDebugMode.isChecked());
        Configuration.getInstance().setDebugTileProviders(checkBoxDebugTileProvider.isChecked());
        Configuration.getInstance().setMapViewHardwareAccelerated(checkBoxHardwareAcceleration.isChecked());
        Configuration.getInstance().setDebugMapTileDownloader(checkBoxDebugDownloading.isChecked());
        Configuration.getInstance().setOsmdroidTileCache(new File(textViewCacheDirectory.getText().toString()));
        Configuration.getInstance().setOsmdroidBasePath(new File(textViewBaseDirectory.getText().toString()));

        try {
            Integer val = Integer.parseInt(zoomSpeedDefault.getText().toString());
            if (val > 0)
                Configuration.getInstance().setAnimationSpeedDefault(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Integer val = Integer.parseInt(zoomSpeedShort.getText().toString());
            if (val > 0)
                Configuration.getInstance().setAnimationSpeedShort(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonManualCacheEntry: {
                showManualEntry(textViewCacheDirectory);
            }
            break;
            case R.id.buttonSetCache: {
                showPickCacheFromList(textViewCacheDirectory, "tiles" + File.separator);
            }
            break;
            case R.id.buttonPurgeCache: {
                purgeCache();
            }
            break;
            case R.id.buttonReset: {
                resetSettings(this);
                abortSave = true;
                finish();
            }
            break;
            case R.id.buttonManualBaseEntry: {
                showManualEntry(textViewBaseDirectory);
            }
            break;
            case R.id.buttonSetBase: {
                showPickCacheFromList(textViewBaseDirectory, "");
            }
            break;


        }
    }

    public static void resetSettings(Context ctx) {
        //delete all preference keys, if you're using this for your own application
        //you may want to consider some additional logic here (only clear osmdroid settings or
        //use something other than the default shared preferences map
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        edit.clear();
        edit.commit();
        //this will repopulate the default settings
        Configuration.setConfigurationProvider(new DefaultConfigurationProvider());
        //this will save the default along with the user agent (important for downloading tiles)
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    private void purgeCache() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SqlTileWriter sqlTileWriter = new SqlTileWriter();
                                boolean b = sqlTileWriter.purgeCache();
                                sqlTileWriter.onDetach();
                                final String title = b ? "SQL Cache purged" : "SQL Cache purge failed, see logcat for details";
                                final int length = b ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
                                PreferenceActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PreferenceActivity.this, title, length).show();
                                    }
                                });
                            }
                        }).start();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.userconfirm).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();

    }

    private void showPickCacheFromList(final TextView tv, final String postfix) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enterCacheLocation);

        final List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList(this);
        List<StorageUtils.StorageInfo> storageListFiltered = new ArrayList<>();
        for (StorageUtils.StorageInfo storageInfo : storageList) {
            if (!storageInfo.readonly) {
                storageListFiltered.add(storageInfo);
            }
        }

        final StorageAdapter arrayAdapter = new StorageAdapter(this, storageListFiltered);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StorageUtils.StorageInfo item = (StorageUtils.StorageInfo) arrayAdapter.getItem(which);
                try {
                    new File(item.path + File.separator + "osmdroid" + File.separator + postfix).mkdirs();
                    tv.setText(item.path + File.separator + "osmdroid" + File.separator + postfix);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(PreferenceActivity.this, "Invalid entry: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void showManualEntry(final TextView textView) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enterCacheLocation);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setLines(1);
        input.setText(textView.getText().toString());
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                File file = new File(input.getText().toString());
                if (!file.exists()) {
                    input.setError("Does not exist");
                } else if (file.exists() && !file.isDirectory()) {
                    input.setError("Not a directory");
                } else if (!StorageUtils.isWritable(file)) {
                    input.setError("Not writable");
                } else {
                    input.setError(null);
                }
            }
        });
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getError() == null) {
                    textView.setText(input.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


}
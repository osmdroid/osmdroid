package org.osmdroid.intro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.osmdroid.intro.StorageAdapter.readableFileSize;

/**
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class StoragePreferenceFragment extends Fragment implements View.OnClickListener {
    Button buttonSetCache,
        buttonManualCacheEntry;
    TextView textViewCacheDirectory,
        textViewCacheMaxSize,
        textViewCacheFreeSpace,
        textViewCacheCurrentSize,
        textViewCacheTrimSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_storage, container, false);

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        textViewCacheDirectory = v.findViewById(R.id.textViewCacheDirectory);
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().toString());

        buttonSetCache = v.findViewById(R.id.buttonSetCache);
        buttonManualCacheEntry = v.findViewById(R.id.buttonManualCacheEntry);
        buttonSetCache.setOnClickListener(this);
        buttonManualCacheEntry.setOnClickListener(this);
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().toString());
        textViewCacheMaxSize = v.findViewById(R.id.textViewCacheMaxSize);
        textViewCacheFreeSpace = v.findViewById(R.id.textViewCacheFreeSpace);
        textViewCacheCurrentSize = v.findViewById(R.id.textViewCacheCurrentSize);
        textViewCacheTrimSize = v.findViewById(R.id.textViewCacheTrimSize);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStorage(getContext());
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().toString());

        textViewCacheMaxSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheMaxBytes()));
        textViewCacheTrimSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheTrimBytes()));

        textViewCacheFreeSpace.setText(readableFileSize(Configuration.getInstance().getOsmdroidTileCache().getFreeSpace()));
        File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
        if (dbFile.exists()) {
            textViewCacheCurrentSize.setText(readableFileSize(dbFile.length()));
        } else {
            textViewCacheCurrentSize.setText("");
        }
    }

    public void updateStorage(Context ctx) {
        //only needed for api23+ since we "should" have had permissions granted by now
        //PreferenceActivity.resetSettings(ctx);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

    }

    // END PERMISSION CHECK
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonManualCacheEntry: {
                showManualEntry();
            }
            break;
            case R.id.buttonSetCache: {
                showPickCacheFromList();
            }
            break;

        }
    }

    private void showPickCacheFromList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(R.string.enterCacheLocation);


        final List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();

        List<StorageUtils.StorageInfo> storageListFiltered = new ArrayList<>();
        for (int i = 0; i < storageList.size(); i++) {
            if (!storageList.get(i).readonly) {
                storageListFiltered.add(storageList.get(i));
            }
        }
        try {
            File f = new File("/data/data/" + getActivity().getPackageName() + "/osmdroid/");
            f.mkdirs();
            StorageUtils.StorageInfo privateStorage = new StorageUtils.StorageInfo(f.getAbsolutePath(), true, false, 0);
            privateStorage.setDisplayName("Application Private Storage");
            storageListFiltered.add(privateStorage);
        } catch (Exception ex) {
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File[] externalFilesDirs = getContext().getExternalFilesDirs(null);
            if (externalFilesDirs != null) {
                //i hate android!
                for (int i = 0; i < externalFilesDirs.length; i++) {
                    File mount = externalFilesDirs[i];
                    if (mount != null && mount.exists() && mount.isDirectory()) {
                        boolean writable = StorageUtils.isWritable(mount);
                        if (writable) {
                            boolean alreadyAdded = false;
                            for (int k = 0; k < storageList.size(); k++) {
                                StorageUtils.StorageInfo storageInfo = storageList.get(k);
                                if (storageInfo.path.equals(mount.getAbsolutePath())) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }
                            if (!alreadyAdded) {
                                StorageUtils.StorageInfo x = new StorageUtils.StorageInfo(mount.getAbsolutePath(), false, false, 0);
                                x.freeSpace = mount.getFreeSpace();
                                storageListFiltered.add(x);
                            }
                        }
                    }
                }
            }
        }

        final StorageAdapter arrayAdapter = new StorageAdapter(this.getContext(), storageListFiltered);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StorageUtils.StorageInfo item = (StorageUtils.StorageInfo) arrayAdapter.getItem(which);
                try {
                    new File(item.path + File.separator + "osmdroid" + File.separator + "tiles" + File.separator).mkdirs();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                textViewCacheDirectory.setText(item.path + File.separator + "osmdroid" + File.separator + "tiles");
                Configuration.getInstance().setOsmdroidTileCache(new File(textViewCacheDirectory.getText() + ""));
                Configuration.getInstance().save(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

                textViewCacheMaxSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheMaxBytes()));
                textViewCacheTrimSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheTrimBytes()));

                textViewCacheFreeSpace.setText(readableFileSize(Configuration.getInstance().getOsmdroidTileCache().getFreeSpace()));
                File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
                if (dbFile.exists()) {
                    textViewCacheCurrentSize.setText(readableFileSize(dbFile.length()));
                } else {
                    textViewCacheCurrentSize.setText("");
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


    private void showManualEntry() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(R.string.enterCacheLocation);

        // Set up the input
        final EditText input = new EditText(this.getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setLines(1);
        input.setText(textViewCacheDirectory.getText().toString());
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
                    textViewCacheDirectory.setText(input.getText().toString());

                    textViewCacheMaxSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheMaxBytes()));
                    textViewCacheTrimSize.setText(readableFileSize(Configuration.getInstance().getTileFileSystemCacheTrimBytes()));

                    textViewCacheFreeSpace.setText(readableFileSize(Configuration.getInstance().getOsmdroidTileCache().getFreeSpace()));
                    File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
                    if (dbFile.exists()) {
                        textViewCacheCurrentSize.setText(readableFileSize(dbFile.length()));
                    } else {
                        textViewCacheCurrentSize.setText("");
                    }
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

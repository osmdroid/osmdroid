package org.osmdroid.intro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.PreferenceActivity;
import org.osmdroid.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class StoragePreferenceFragment extends Fragment implements View.OnClickListener {
    Button buttonSetCache,
        buttonManualCacheEntry;
    TextView textViewCacheDirectory;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_storage,container,false);

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        textViewCacheDirectory= (TextView) v.findViewById(R.id.textViewCacheDirectory);
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().toString());

        buttonSetCache = (Button) v.findViewById(R.id.buttonSetCache);
        buttonManualCacheEntry = (Button) v.findViewById(R.id.buttonManualCacheEntry);
        buttonSetCache.setOnClickListener(this);
        buttonManualCacheEntry.setOnClickListener(this);




        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        //only needed for api23+ since we "should" have had permissions granted by now
        PreferenceActivity.resetSettings(this.getContext());
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        textViewCacheDirectory.setText(Configuration.getInstance().getOsmdroidTileCache().toString());
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

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.select_dialog_singlechoice);

        final List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();
        for (int i = 0; i < storageList.size(); i++) {
            if (!storageList.get(i).readonly)
                arrayAdapter.add(storageList.get(i).path);
        }

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = arrayAdapter.getItem(which);
                try {
                    new File(item + File.separator + "osmdroid" + File.separator + "tiles" + File.separator).mkdirs();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                textViewCacheDirectory.setText(item + File.separator + "osmdroid" + File.separator + "tiles");
                Configuration.getInstance().setOsmdroidTileCache(new File(textViewCacheDirectory.getText()+""));
                Configuration.getInstance().save(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
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

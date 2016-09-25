package org.osmdroid.samplefragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 9/25/16.
 */

public class CacheImport extends BaseSampleFragment implements View.OnClickListener, Runnable {

    Button btnCache;

    @Override
    public String getSampleTitle() {
        return "Import the file system cache into the newer sql cache";
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        btnCache = (Button) root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Cache Filesystem Import");

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:
                new Thread(this).start();
                break;

        }
    }

    @Override
    public void run() {
        IFilesystemCache tileWriter = mMapView.getTileProvider().getTileWriter();
        if (tileWriter instanceof SqlTileWriter){
            final int[] b = ((SqlTileWriter) tileWriter).importFromFileCache(true);
            if (getActivity()!=null){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Toast.makeText(getActivity(), "Cache Import success/failures/default/failres " + b[0] + "/" + b[1] + "/" + b[2] + "/" + b[3], Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}

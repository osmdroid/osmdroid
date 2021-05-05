package org.osmdroid.samplefragments.cache;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 9/25/16.
 */

public class CachePurge extends BaseSampleFragment implements View.OnClickListener, Runnable {
    Button btnCache;

    @Override
    public String getSampleTitle() {
        return "How to purge the tile cache";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Cache Purge (database)");

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
        if (tileWriter instanceof SqlTileWriter) {
            final boolean b = ((SqlTileWriter) tileWriter).purgeCache();
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (b)
                            Toast.makeText(getActivity(), "Cache Purge successful", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Cache Purge failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
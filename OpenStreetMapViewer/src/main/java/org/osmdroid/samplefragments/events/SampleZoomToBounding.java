package org.osmdroid.samplefragments.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 10/4/16.
 */
public class SampleZoomToBounding extends BaseSampleFragment implements View.OnClickListener {
    Button btnCache;
    @Override
    public String getSampleTitle() {
        return "Zoom to Bounding Box";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        btnCache = (Button) root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Zoom to bounds");

        return root;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:
                BoundingBox box = new BoundingBox(47d, -76d, 46d, -77d);
                mMapView.zoomToBoundingBox(box, true);
                break;

        }
    }

}

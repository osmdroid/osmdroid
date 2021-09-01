package org.osmdroid.samplefragments.cache;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
 * An example of importing stored on disk cache produced by osmdroid < 5.4 using the older TileWriter
 * class
 *
 * @see org.osmdroid.tileprovider.modules.TileWriter
 * @see SqlTileWriter
 * Created by alex on 9/25/16.
 */

public class CacheImport extends BaseSampleFragment implements View.OnClickListener, Runnable {

    boolean removeFromFileSystem = true;
    Button btnCache;

    @Override
    public String getSampleTitle() {
        return "Import the file system cache into the newer sql cache";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        mMapView = new MapView(getActivity());
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        btnCache.setText("Cache Filesystem Import");

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCache:

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                removeFromFileSystem = false;
                                break;
                        }
                        new Thread(CacheImport.this).start();

                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Would you like to remove the tiles from the file system after importing into the cache database?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


                break;

        }
    }

    @Override
    public void run() {
        final IFilesystemCache tileWriter = mMapView.getTileProvider().getTileWriter();
        if (tileWriter instanceof SqlTileWriter) {
            final int[] b = ((SqlTileWriter) tileWriter).importFromFileCache(removeFromFileSystem);
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Cache Import success/failures/default/failres " + b[0] + "/" + b[1] + "/" + b[2] + "/" + b[3], Toast.LENGTH_LONG).show();
                    }
                });
            }


        }
    }
}

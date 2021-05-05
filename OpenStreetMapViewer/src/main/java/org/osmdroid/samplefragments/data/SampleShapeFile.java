package org.osmdroid.samplefragments.data;

import android.graphics.Paint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PolyOverlayWithIW;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simple how to for importing and display an ESRI shape file
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 */

public class SampleShapeFile extends SampleMapEventListener {


    @Override
    public String getSampleTitle() {
        return "Shape File Import";
    }

    final int MENU_ADD_SHAPE = Menu.FIRST;
    final int MENU_ADD_BOUNDS = MENU_ADD_SHAPE + 1;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ADD_SHAPE, Menu.NONE, "Import a shape file");
        menu.add(0, MENU_ADD_BOUNDS, Menu.NONE, "Draw bounds");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_SHAPE:
                showPicker();
                return true;
            case MENU_ADD_BOUNDS:
                List<GeoPoint> pts = new ArrayList<>();
                BoundingBox boundingBox = mMapView.getBoundingBox();
                pts.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()));
                pts.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast()));
                pts.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()));
                pts.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest()));
                pts.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()));
                Polygon bounds = new Polygon(mMapView);
                bounds.setPoints(pts);
                bounds.setSubDescription(boundingBox.toString());
                // bounds.setStrokeColor(Color.RED);
                mMapView.getOverlayManager().add(bounds);
                mMapView.invalidate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.invalidate();
    }

    private void showPicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);

        Set<String> registeredExtensions = ArchiveFileFactory.getRegisteredExtensions();
        registeredExtensions.add("shp");


        String[] ret = new String[registeredExtensions.size()];
        ret = registeredExtensions.toArray(ret);
        properties.extensions = ret;

        FilePickerDialog dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                try {
                    List<Overlay> folder = ShapeConverter.convert(mMapView, new File(files[0]));
                    for (final Overlay item : folder) {
                        if (item instanceof PolyOverlayWithIW) {
                            final PolyOverlayWithIW poly = (PolyOverlayWithIW) item;
                            poly.setDowngradePixelSizes(50, 25);
                            poly.setDowngradeDisplay(true);
                            final Paint paint = poly.getOutlinePaint();
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeJoin(Paint.Join.ROUND);
                            paint.setStrokeCap(Paint.Cap.ROUND);
                        }
                    }
                    mMapView.getOverlayManager().addAll(folder);
                    mMapView.invalidate();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error importing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "error importing file from " + files[0], e);
                }

            }

        });
        dialog.show();

    }
}

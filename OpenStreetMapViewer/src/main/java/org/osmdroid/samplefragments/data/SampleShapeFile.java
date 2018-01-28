package org.osmdroid.samplefragments.data;

import android.os.Build;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.File;
import java.util.Set;

/**
 * created on 1/28/2018.
 *
 * @author Alex O'Ree
 */

public class SampleShapeFile extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Shape File Import";
    }
    final int MENU_ADD_SHAPE= Menu.FIRST;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ADD_SHAPE, Menu.NONE, "Import a shape file");
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
        }
        return super.onOptionsItemSelected(item);
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
                    FolderOverlay folder = ShapeConverter.convert(mMapView, new File(files[0]));
                    mMapView.getOverlayManager().add(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
        dialog.show();

    }
}

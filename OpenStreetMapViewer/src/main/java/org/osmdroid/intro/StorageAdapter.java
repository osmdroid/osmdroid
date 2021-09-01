package org.osmdroid.intro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * created on 1/18/2017.
 *
 * @author Alex O'Ree
 */

public class StorageAdapter extends ArrayAdapter {
    List<StorageUtils.StorageInfo> data;

    public StorageAdapter(Context context, List<StorageUtils.StorageInfo> data) {
        super(context, R.layout.layout_storage_device);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int id) {
        return data.get(id);
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_storage_device, parent, false);
        }
        StorageUtils.StorageInfo info = (StorageUtils.StorageInfo) getItem(position);

        if (info != null) {
            // Find fields to populate in inflated template
            TextView drive = convertView.findViewById(R.id.storageName);
            TextView frespace = convertView.findViewById(R.id.storageFreespace);
            TextView path = convertView.findViewById(R.id.storagePath);
            drive.setText(info.getDisplayName());
            frespace.setText("Free space: " + readableFileSize(info.freeSpace));
            path.setText(info.path);

        }


        return convertView;
    }

}

package org.osmdroid.samplefragments.layers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.OverlayWithIW;

/**
 * created on 2/18/2018.
 *
 * @author Alex O'Ree
 */

public class OverlayAdapter extends ArrayAdapter {
    OverlayManager manager;
    Context context = null;

    public OverlayAdapter(@NonNull Context context, OverlayManager manager) {
        super(context, R.layout.drawer_list_item);
        this.manager = manager;
        this.context = context;
    }

    @Override
    public int getCount() {

        synchronized (manager) {
            if (manager != null)
                return manager.size();
            return 0;
        }
    }

    public Overlay getItem(int position) {
        return manager.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        TextView view = rowView.findViewById(R.id.itemText);

        Overlay overlay = getItem(position);
        if (overlay != null) {
            if (overlay instanceof OverlayWithIW) {
                String title = ((OverlayWithIW) overlay).getTitle();
                if (title == null || title.length() == 0)
                    title = overlay.getClass().getSimpleName();
                view.setText(title);
            } else view.setText(overlay.getClass().getSimpleName());
        }
        return rowView;
    }
}

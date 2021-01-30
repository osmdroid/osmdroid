package org.osmdroid.samplefragments.milstd2525;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;

/**
 * created on 1/15/2018.
 *
 * @author Alex O'Ree
 */

public class MilStdAdapter extends ArrayAdapter<SimpleSymbol> implements Filterable, Comparator<SimpleSymbol> {
    List<SimpleSymbol> values = new ArrayList<>();

    String charAffil = "F";
    Context context = null;
    float density = 240;

    public MilStdAdapter(Context context) {
        super(context, R.layout.milstd2525searchitem);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        resetSymbols();

        this.context = context;
    }

    private void resetSymbols() {
        synchronized (values) {
            values.clear();
            Map<String, SymbolDef> stringSymbolDefMap = SymbolDefTable.getInstance().GetAllSymbolDefs(RendererSettings.getInstance().getSymbologyStandard());
            for (SymbolDef def : stringSymbolDefMap.values()) {
                SimpleSymbol from = SimpleSymbol.createFrom(def);
                if (from.canDraw())
                    values.add(SimpleSymbol.createFrom(def));
            }

            Map<String, UnitDef> allUnitDefs = UnitDefTable.getInstance().getAllUnitDefs(RendererSettings.getInstance().getSymbologyStandard());
            for (UnitDef def : allUnitDefs.values()) {
                SimpleSymbol from = SimpleSymbol.createFrom(def);
                if (from.canDraw())
                    values.add(from);
            }
            Collections.sort(values, this);
        }
    }

    @Override
    public int getCount() {

        synchronized (this) {
            if (values != null)
                return values.size();
            return 0;
        }
    }

    public SimpleSymbol getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.milstd2525searchitem, parent, false);
        ImageView milstd_search_result_preview = rowView.findViewById(R.id.milstd_search_result_preview);
        TextView milstd_search_result_title = rowView.findViewById(R.id.milstd_search_result_title);
        TextView milstd_search_result_hierarchy = rowView.findViewById(R.id.milstd_search_result_hierarchy);
        TextView milstd_search_result_description = rowView.findViewById(R.id.milstd_search_result_description);

        SimpleSymbol def = getItem(position);

        if (def.getDescription() != null)
            milstd_search_result_description.setText(def.getDescription());
        if (def.getBasicSymbolId() != null) {
            milstd_search_result_title.setText(def.getBasicSymbolId());
        }
        if (def.getHierarchy() != null)
            milstd_search_result_hierarchy.setText(def.getHierarchy());

        SparseArray<String> attr = new SparseArray<>();
        attr.put(MilStdAttributes.PixelSize, (int) (45 * density) + "");
        attr.put(MilStdAttributes.DrawAsIcon, "true");

        String code = def.getBasicSymbolId();
        if (code.charAt(1) == '*') {
            code = code.substring(0, 1) + charAffil + code.substring(2);
        }
        //TODO mobility, country code, status, etc


        ImageInfo ii = MilStdIconRenderer.getInstance().RenderIcon(code, new SparseArray<String>(), attr);
        if (ii != null) {
            Drawable d = new BitmapDrawable(ii.getImage());
            milstd_search_result_preview.setImageDrawable(d);
        }
        return rowView;
    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            values = (ArrayList<SimpleSymbol>) results.values;
            notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // NOTE: this function is *always* called from a background thread, and
            // not the UI thread.
            FilterResults results = new FilterResults();
            ArrayList<SimpleSymbol> filteredArrayNames = new ArrayList<SimpleSymbol>();

            resetSymbols();

            // perform your search here using the searchConstraint String.
            if (constraint == null || constraint.length() == 0) {

                results.values = values;
                results.count = values.size();
            } else {
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < values.size(); i++) {
                    try {
                        SimpleSymbol dataNames = values.get(i);
                        if (dataNames != null && dataNames.getDescription() != null && dataNames.getDescription().toLowerCase().contains(constraint)) {
                            filteredArrayNames.add(dataNames);
                        }
                    } catch (Exception ex) {
                        break;
                    }
                }

                results.count = filteredArrayNames.size();
                results.values = filteredArrayNames;
            }

            return results;
        }
    };

    @Override
    public int compare(SimpleSymbol lhs, SimpleSymbol rhs) {
        if (lhs == null) return 0;
        if (lhs.getDescription() == null)
            lhs.setDescription("");
        if (rhs == null) return 0;
        if (rhs.getDescription() == null)
            rhs.setDescription("");
        return lhs.getDescription().compareTo(rhs.getDescription());
    }

    public void update(String charAffiliation) {
        this.charAffil = charAffiliation;
        notifyDataSetChanged();

    }
}

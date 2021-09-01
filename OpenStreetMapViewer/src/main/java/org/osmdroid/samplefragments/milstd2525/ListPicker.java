package org.osmdroid.samplefragments.milstd2525;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import org.osmdroid.R;

import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;

/**
 * created on 1/15/2018.
 *
 * @author Alex O'Ree
 */

public class ListPicker implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher {

    public interface Callback {
        public void selected(SimpleSymbol def);
    }

    public ListPicker(Callback cb) {
        this.cb = cb;
    }

    Callback cb = null;
    AlertDialog picker = null;
    Button milstd_search_cancel = null;
    ListView milstd_search_results = null;
    EditText milstd_search = null;
    RadioButton milstd_search_affil_f = null;
    RadioButton milstd_search_affil_h = null;
    RadioButton milstd_search_affil_n = null;
    RadioButton milstd_search_affil_u = null;

    String charAffiliation = "F";

    public void destroy() {
        if (picker != null) {
            picker.dismiss();
        }
        picker = null;
        cb = null;
        milstd_search_cancel = null;
        milstd_search_results = null;
        milstd_search = null;
    }

    public void show(Activity activity) {
        if (picker != null) {
            picker.show();
            return;
        }
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, R.layout.milstd2525list, null);

        milstd_search_affil_f = view.findViewById(R.id.milstd_search_affil_f);
        milstd_search_affil_h = view.findViewById(R.id.milstd_search_affil_h);
        milstd_search_affil_n = view.findViewById(R.id.milstd_search_affil_n);
        milstd_search_affil_u = view.findViewById(R.id.milstd_search_affil_u);

        milstd_search_affil_f.setOnClickListener(this);
        milstd_search_affil_h.setOnClickListener(this);
        milstd_search_affil_n.setOnClickListener(this);
        milstd_search_affil_u.setOnClickListener(this);

        milstd_search = view.findViewById(R.id.milstd_search);
        milstd_search.addTextChangedListener(this);
        milstd_search_results = view.findViewById(R.id.milstd_search_results);
        milstd_search_results.setAdapter(new MilStdAdapter(activity));
        milstd_search_results.setOnItemClickListener(this);

        milstd_search_cancel = view.findViewById(R.id.milstd_search_cancel);
        milstd_search_cancel.setOnClickListener(this);


        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                picker.dismiss();
            }
        });
        picker = builder.create();
        picker.show();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.milstd_search_cancel:
                picker.dismiss();
                break;
            //TODO redraw all the icons?
            case R.id.milstd_search_affil_f:
                charAffiliation = "F";
                ((MilStdAdapter) milstd_search_results.getAdapter()).update(charAffiliation);
                break;
            case R.id.milstd_search_affil_h:
                charAffiliation = "H";
                ((MilStdAdapter) milstd_search_results.getAdapter()).update(charAffiliation);
                break;
            case R.id.milstd_search_affil_n:
                charAffiliation = "N";
                ((MilStdAdapter) milstd_search_results.getAdapter()).update(charAffiliation);

                break;
            case R.id.milstd_search_affil_u:
                charAffiliation = "U";
                ((MilStdAdapter) milstd_search_results.getAdapter()).update(charAffiliation);

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SimpleSymbol def = (SimpleSymbol) parent.getItemAtPosition(position);
        if (cb != null) {

            //TODO this is a good place to show another dialog enabling the user to
            //symbol modifiers and attributes
            //modifiers are symbol specific
            //attributes are static and relatively simple
            if (def.getSymbolCode().startsWith("G") || def.getSymbolCode().startsWith("W")) {
                if (SymbolUtilities.canSymbolHaveModifier(def.getBasicSymbolId(), ModifiersTG.A_SYMBOL_ICON, RendererSettings.getInstance().getSymbologyStandard())) {
                    //render some text input
                }
                //etc
            } else {
                if (SymbolUtilities.canSymbolHaveModifier(def.getBasicSymbolId(), ModifiersUnits.A_SYMBOL_ICON, RendererSettings.getInstance().getSymbologyStandard())) {
                    //render some text input
                }
                //etc
            }


            picker.dismiss();
            String code = def.getBasicSymbolId();
            if (code.charAt(1) == '*') {
                code = code.substring(0, 1) + charAffiliation + code.substring(2);
            }
            def.setSymbolCode(code);
            cb.selected(def);

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ((MilStdAdapter) milstd_search_results.getAdapter()).getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
        ((MilStdAdapter) milstd_search_results.getAdapter()).getFilter().filter(s);
    }
}

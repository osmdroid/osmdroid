package org.osmdroid.samplefragments.milstd2525;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.text.DecimalFormat;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;

/**
 * A sample that provides two ways to plot single point MIL-STD 2525 icons
 *
 * <ul>
 * <li>Via direct user input, enter the symbol code, then the icon is plotted at screen center</li>
 * <li>Via searchable picker, icons can then be plotted via long press</li>
 * </ul>
 * <p>
 * TODO
 * <ul>
 * <li>More support for modifiers and attributes</li>
 * <li>Multipoint symbols</li>
 * </ul>
 * created on 12/22/2017.
 *
 * @author Alex O'Ree
 */

public class Plotter extends SampleGridlines implements View.OnClickListener, TextWatcher, ListPicker.Callback {
    public static final DecimalFormat df = new DecimalFormat("#.000000");
    private final int MENU_ADD_POINT = Menu.FIRST;
    private final int MENU_ADD_VIA_PICKER = MENU_ADD_POINT + 1;
    MilStdIconRenderer mir = null;
    ImageButton painting, panning;
    MilStdCustomPaintingSurface paint;
    TextView textViewCurrentLocation;
    SimpleSymbol lastSelectedSymbol = null;
    MilStdPointPlottingOverlay plotter = new MilStdPointPlottingOverlay();
    AlertDialog picker = null;
    TextView canRender = null;
    EditText symbolCode = null;

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================
    EditText symbolSize = null;
    RadioButton radio_milstd2525c = null;
    RadioButton radio_milstd2525b = null;
    Button addIcon = null;
    Button cancelAddIcon = null;
    int dpi = 0;

    public Plotter() {
        //init the renderer

        RendererSettings.getInstance().setSymbologyStandard(RendererSettings.Symbology_2525C);
        //Next lines are mandatory.  These tell the renderer where the cache folder is located which is needed to process the embedded xml files.
        mir = MilStdIconRenderer.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_with_location_milstd, container, false);

        mMapView = v.findViewById(R.id.mapview);
        textViewCurrentLocation = v.findViewById(R.id.textViewCurrentLocation);
        panning = v.findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(Color.BLACK);
        painting = v.findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpi = metrics.densityDpi;
        paint = v.findViewById(R.id.paintingSurface);
        paint.init(mMapView);
        return v;
    }

    @Override
    public String getSampleTitle() {
        return "Symbol Plotter";
    }


    @Override
    public void addOverlays() {
        super.addOverlays();

        String cacheDir = getActivity().getApplicationContext().getCacheDir().getAbsoluteFile().getAbsolutePath();
        mir.init(getContext(), cacheDir);
        mMapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });
        mMapView.getController().setZoom(15f);
        mMapView.getController().setCenter(new GeoPoint(41.0, -77.0));
        updateInfo();
        mMapView.getOverlayManager().add(plotter);
    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(
                (plotter.def != null ? plotter.def.getSymbolCode() + "\n" : "") +
                        df.format(mapCenter.getLatitude()) + "," +
                        df.format(mapCenter.getLongitude())
                        + ",zoom=" + mMapView.getZoomLevelDouble());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ADD_POINT, Menu.NONE, "Add a symbol by code");
        menu.add(0, MENU_ADD_VIA_PICKER, Menu.NONE, "Add a symbol by picker");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_POINT:
                showPicker();
                return true;
            case MENU_ADD_VIA_PICKER:
                showSelector();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showSelector() {
        //opens a dialog with a searchable list view of all single point symbols
        ListPicker picker = new ListPicker(this);
        picker.show(getActivity());
    }

    private void showPicker() {
        if (picker != null) {
            picker.show();
            return;
        }
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.milstd2525single, null);


        canRender = view.findViewById(R.id.canRender);
        symbolCode = view.findViewById(R.id.symbolCode);
        symbolCode.addTextChangedListener(this);
        symbolSize = view.findViewById(R.id.symbolSize);
        radio_milstd2525c = view.findViewById(R.id.radio_milstd2525c);
        radio_milstd2525b = view.findViewById(R.id.radio_milstd2525b);
        radio_milstd2525b.setOnClickListener(this);
        radio_milstd2525c.setOnClickListener(this);
        addIcon = view.findViewById(R.id.addIcon);
        addIcon.setOnClickListener(this);
        addIcon.setEnabled(false);

        cancelAddIcon = view.findViewById(R.id.cancelAddIcon);
        cancelAddIcon.setOnClickListener(this);


        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        symbolCode.setText(defaultSharedPreferences.getString("MILSTDCODE", "SFGPUCI-----US-"));
        symbolSize.setText(defaultSharedPreferences.getInt("MILSTDSIZE", 128) + "");

        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closePicker();
            }
        });
        picker = builder.create();
        picker.show();
        validateSymbolCode(symbolCode.getText().toString());

    }

    @Override
    public void onPause() {
        super.onPause();
        closePicker();
    }

    private void closePicker() {
        if (picker != null)
            picker.dismiss();
        picker = null;

        canRender = null;
        symbolCode = null;
        symbolSize = null;
        radio_milstd2525c = null;
        radio_milstd2525b = null;
        addIcon = null;
        cancelAddIcon = null;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radio_milstd2525b:
            case R.id.radio_milstd2525c:
                if (((RadioButton) v).isChecked()) {
                    RendererSettings.getInstance().setSymbologyStandard(RendererSettings.Symbology_2525C);
                } else
                    RendererSettings.getInstance().setSymbologyStandard(RendererSettings.Symbology_2525B);
                break;
            case R.id.cancelAddIcon:
                picker.dismiss();
                break;
            case R.id.addIcon:
                //from the menu, user entered code
                String code = symbolCode.getText().toString();
                int size = 128;
                try {
                    size = Integer.parseInt(symbolSize.getText().toString());
                } catch (Exception ex) {
                }
                String baseCode = SymbolUtilities.getBasicSymbolID(code);
                SymbolDef def = SymbolDefTable.getInstance().getSymbolDef(baseCode, RendererSettings.getInstance().getSymbologyStandard());

                SparseArray<String> attr = new SparseArray<>();
                attr.put(MilStdAttributes.PixelSize, size + "");

                ImageInfo ii = mir.RenderIcon(code, new SparseArray<String>(), attr);
                Marker m = new Marker(mMapView);
                m.setPosition((GeoPoint) mMapView.getMapCenter());
                m.setTitle(code);
                if (def != null) {
                    m.setSubDescription(def.getFullPath());
                    m.setSnippet(def.getDescription() + "\n" + def.getHierarchy());
                }
                Drawable d = new BitmapDrawable(ii.getImage());
                m.setImage(d);
                m.setIcon(d);
                int centerX = ii.getCenterPoint().x;    //pixel center position
                //calculate what percentage of the center this value is
                float realCenterX = (float) centerX / (float) ii.getImage().getWidth();

                int centerY = ii.getCenterPoint().y;
                float realCenterY = (float) centerY / (float) ii.getImage().getHeight();
                m.setAnchor(realCenterX, realCenterY);
                mMapView.getOverlayManager().add(m);
                mMapView.invalidate();
                picker.dismiss();

                //TODO store the symbol code and size as an android preference
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                edit.putString("MILSTDCODE", code);
                RendererSettings.getInstance().setDefaultPixelSize(size);
                edit.putInt("MILSTDSIZE", size);
                edit.commit();

                break;
            case R.id.enablePanning:
                enablePanning();

                break;
            case R.id.enablePainting:
                enablePainting();
                break;
        }
    }

    private void enablePanning() {
        paint.setVisibility(View.GONE);
        panning.setBackgroundColor(Color.BLACK);
        painting.setBackgroundColor(Color.TRANSPARENT);
    }

    private void enablePainting() {
        paint.setVisibility(View.VISIBLE);
        painting.setBackgroundColor(Color.BLACK);
        panning.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        validateSymbolCode(s.toString());

    }

    private void validateSymbolCode(String code) {
        //validate that the input is correct

        if (code == null || code.length() == 15) {
            if (mir.CanRender(code, new SparseArray<String>(), new SparseArray<String>())) {
                canRender.setText("");
                addIcon.setEnabled(true);
            } else {
                canRender.setText("Invalid Input.");
                addIcon.setEnabled(false);
            }
        } else {
            canRender.setText("Wrong length, must be 15 characters.");
            addIcon.setEnabled(false);
        }

    }

    @Override
    public void selected(SimpleSymbol def) {
        if (def == null) {
            enablePanning();
        }
        if (def.canDraw()) {
            ModifierPicker picker = new ModifierPicker();
            picker.show(getActivity(), def);

            if (def.getMaxPoints() == 1) {
                enablePanning();
                plotter.setSymbol(def);
                Toast.makeText(getActivity(), "Long press to plot!", Toast.LENGTH_SHORT).show();
            }
            if (def.getMinPoints() > 1) {
                enablePainting();
                paint.setSymbol(def);
                Toast.makeText(getActivity(), "Draw on the screen!", Toast.LENGTH_SHORT).show();
            }
        } else {
            enablePanning();
            Toast.makeText(getActivity(), "Symbol cannot be plotted, try another!", Toast.LENGTH_SHORT).show();
        }

    }
}

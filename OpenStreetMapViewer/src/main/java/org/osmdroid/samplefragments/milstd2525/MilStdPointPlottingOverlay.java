package org.osmdroid.samplefragments.milstd2525;

import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;

/**
 * A super simple overlay to plot a marker when the user long presses on the map.
 * <p>
 * It does not draw anything on screen but does intercept long press events then adds
 * a hardcoded Marker to the map
 * created on 11/19/2017.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class MilStdPointPlottingOverlay extends Overlay {

    public MilStdPointPlottingOverlay() {
        super();
    }

    SimpleSymbol def = null;

    public void setSymbol(SimpleSymbol def) {
        this.def = def;
    }

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        if (def != null) {
            GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY(), null);

            //just in case the point is off the map, let's fix the coordinates
            if (pt.getLongitude() < -180)
                pt.setLongitude(pt.getLongitude() + 360);
            if (pt.getLongitude() > 180)
                pt.setLongitude(pt.getLongitude() - 360);
            //latitude is a bit harder. see https://en.wikipedia.org/wiki/Mercator_projection
            if (pt.getLatitude() > mapView.getTileSystem().getMaxLatitude())
                pt.setLatitude(mapView.getTileSystem().getMaxLatitude());
            if (pt.getLatitude() < mapView.getTileSystem().getMinLatitude())
                pt.setLatitude(mapView.getTileSystem().getMinLatitude());

            String code = def.getSymbolCode().replace("*", "-");
            //TODO if (!def.isMultiPoint())
            {
                int size = 128;

                SparseArray<String> attr = new SparseArray<>();
                attr.put(MilStdAttributes.PixelSize, size + "");

                ImageInfo ii = MilStdIconRenderer.getInstance().RenderIcon(code, def.getModifiers(), attr);
                Marker m = new Marker(mapView);
                m.setPosition(pt);
                m.setTitle(code);
                m.setSnippet(def.getDescription() + "\n" + def.getHierarchy());
                m.setSubDescription(def.getPath() + "\n" + m.getPosition().getLatitude() + "," + m.getPosition().getLongitude());

                if (ii != null && ii.getImage() != null) {
                    BitmapDrawable d = new BitmapDrawable(ii.getImage());
                    m.setImage(d);
                    m.setIcon(d);

                    int centerX = ii.getCenterPoint().x;    //pixel center position
                    //calculate what percentage of the center this value is
                    float realCenterX = (float) centerX / (float) ii.getImage().getWidth();

                    int centerY = ii.getCenterPoint().y;
                    float realCenterY = (float) centerY / (float) ii.getImage().getHeight();
                    m.setAnchor(realCenterX, realCenterY);


                    mapView.getOverlayManager().add(m);
                    mapView.invalidate();
                }
            }

            return true;
        }
        return false;
    }
}

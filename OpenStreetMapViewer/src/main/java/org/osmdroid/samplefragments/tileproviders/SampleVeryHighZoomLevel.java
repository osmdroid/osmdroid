package org.osmdroid.samplefragments.tileproviders;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ScaleBarOverlay;

/**
 * A lousy example of very high zoom levels.
 * A nicer example would require very high zoom level tiles.
 *
 * @author Fabrice Fontaine
 * @since 6.0.0
 */
public class SampleVeryHighZoomLevel extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Offline abstract tiles for zoom levels 0 to 29";
    }

    @Override
    public void addOverlays() {
        mMapView.setUseDataConnection(false);

        final ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mMapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(200, 10);
        mMapView.getOverlays().add(scaleBarOverlay);

        final ITileSource tileSource = new XYTileSource(
                "Abstract", 0, 29, 256, ".png", new String[]{"http://localhost/"}, "abstract data");
        mMapView.setUseDataConnection(false);

        final MapTileAssetsProvider assetsProvider = new MapTileAssetsProvider(new SimpleRegisterReceiver(getContext()), getActivity().getAssets(), tileSource);

        final MapTileApproximater approximationProvider = new MapTileApproximater();
        approximationProvider.addProvider(assetsProvider);

        final MapTileProviderArray array = new MapTileProviderArray(
                tileSource, new SimpleRegisterReceiver(getContext()),
                new MapTileModuleProviderBase[]{assetsProvider, approximationProvider});

        mMapView.setTileProvider(array);

        mMapView.getController().setZoom(29.);
        // cf. https://fr.wikipedia.org/wiki/Point_z%C3%A9ro_des_routes_de_France
        // In English: starting point of all French roads
        mMapView.setExpectedCenter(new GeoPoint(48.85340215825712, 2.348784611094743));
        mMapView.invalidate();
    }
}

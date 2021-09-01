package org.osmdroid.samplefragments.data;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.SpeechBalloonOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo around the new {@link SpeechBalloonOverlay} feature
 *
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class SampleSpeechBalloon extends BaseSampleFragment {

    private final List<GeoPoint> mGeoPoints = new ArrayList<>();
    private final Paint mBackground = new Paint();
    private final Paint mForeground = new Paint();
    private final Paint mDragBackground = new Paint();
    private final Paint mDragForeground = new Paint();

    @Override
    public String getSampleTitle() {
        return "Speech Balloon";
    }

    private BitmapDrawable mBitmapDrawable;

    @Override
    public void addOverlays() {
        super.addOverlays();

        final int radius = 10;
        final Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(radius, radius, radius, paint);
        mBitmapDrawable = new BitmapDrawable(bitmap);

        mBackground.setStyle(Paint.Style.FILL);
        mBackground.setColor(Color.WHITE);
        mForeground.setStyle(Paint.Style.STROKE);
        mForeground.setColor(Color.BLACK);
        mForeground.setTextSize(30);
        mForeground.setAntiAlias(true);
        mDragBackground.setStyle(Paint.Style.FILL);
        mDragBackground.setColor(Color.YELLOW);
        mDragForeground.setStyle(Paint.Style.STROKE);
        mDragForeground.setColor(Color.RED);
        mDragForeground.setTextSize(30);
        mDragForeground.setAntiAlias(true);

        add(new POI("Long click and drag me", new GeoPoint(43.1677094, -1.23698415), -300, -90));
        add(new POI("Roncesvalles", new GeoPoint(43.01774243892033, -1.317764479899253)));
        add(new POI("Urdániz", new GeoPoint(42.9304266, -1.50463709)));
        add(new POI("Pamplona", new GeoPoint(42.81116477962334, -1.649884335366608), -200, -50));
        add(new POI("Puente la Reina", new GeoPoint(42.66585898113284, -1.815904950575316)));
        add(new POI("Estella", new GeoPoint(42.67372296488218, -2.025552547253327)));
        add(new POI("Los Arcos", new GeoPoint(42.5651743819995, -2.187210645317038)));
        add(new POI("Logroño", new GeoPoint(42.46552987114763, -2.445282148422933), 0, 90));
        add(new POI("Nájera", new GeoPoint(42.41652176456041, -2.732803767417607)));
        add(new POI("Santo Domingo de la Calzada", new GeoPoint(42.43229304715269, -2.952542527566706)));
        add(new POI("Belorado", new GeoPoint(42.4262676963629, -3.184220120411581)));
        add(new POI("Agés", new GeoPoint(42.369722, -3.4794)));
        add(new POI("Burgos", new GeoPoint(42.35092384897927, -3.685218770505309), -30, 90));
        add(new POI("Hontanas", new GeoPoint(42.316666, -4.033333)));
        add(new POI("Boadilla del Camino", new GeoPoint(42.25, -4.35)));
        add(new POI("Carrion de los Condes", new GeoPoint(42.33881483100247, -4.595917714974391)));
        add(new POI("Terradillos de los Templarios", new GeoPoint(42.362777, -4.8902777)));
        add(new POI("El Burgo Ranero", new GeoPoint(42.41746731921432, -5.218695473589733)));
        add(new POI("León", new GeoPoint(42.60054247433525, -5.572908186230237), 0, -100));
        add(new POI("Villar de Mazarife", new GeoPoint(42.483611, -5.7316666)));
        add(new POI("Astorga", new GeoPoint(42.44981716013144, -6.049581358750089)));
        add(new POI("Foncebadón", new GeoPoint(42.4916666, -6.3425)));
        add(new POI("Ponferrada", new GeoPoint(42.54629790350737, -6.578190951631911)));
        add(new POI("Trabadelo", new GeoPoint(42.6494444, -6.88194444)));
        add(new POI("Fonfría", new GeoPoint(42.73138888, -7.15694444)));
        add(new POI("Barbadelo", new GeoPoint(42.766666, -7.45)));
        add(new POI("Hospital da Cruz", new GeoPoint(42.840555, -7.735)));
        add(new POI("Melide", new GeoPoint(42.916666, -8.016666)));
        add(new POI("Pedrouzo", new GeoPoint(42.904444, -8.3625)));
        add(new POI("Santiago de Compostela", new GeoPoint(42.87968184500255, -8.545971242146521), 0, 150));

        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(mGeoPoints);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(boundingBox, false, 50);
            }
        });
    }

    private void add(final POI pPOI) {
        mGeoPoints.add(pPOI.mGeoPoint);
        addToDisplay(pPOI);
    }

    private void addToDisplay(final POI pPOI) {
        final Marker marker = new Marker(mMapView);
        marker.setTitle(pPOI.mTitle);
        marker.setPosition(pPOI.mGeoPoint);
        marker.setIcon(mBitmapDrawable);
        mMapView.getOverlays().add(marker);
        if (pPOI.mSpeechBalloon) {
            final SpeechBalloonOverlay speechBalloonOverlay = new SpeechBalloonOverlay();
            speechBalloonOverlay.setTitle(pPOI.mTitle);
            speechBalloonOverlay.setMargin(10);
            speechBalloonOverlay.setRadius(15);
            speechBalloonOverlay.setGeoPoint(new GeoPoint(pPOI.mGeoPoint));
            speechBalloonOverlay.setOffset(pPOI.mOffsetX, pPOI.mOffsetY);
            speechBalloonOverlay.setForeground(mForeground);
            speechBalloonOverlay.setBackground(mBackground);
            speechBalloonOverlay.setDragForeground(mDragForeground);
            speechBalloonOverlay.setDragBackground(mDragBackground);
            mMapView.getOverlays().add(speechBalloonOverlay);
        }
    }

    private class POI {
        private String mTitle;
        private GeoPoint mGeoPoint;
        private boolean mSpeechBalloon;
        private int mOffsetX;
        private int mOffsetY;

        private POI(String pTitle, GeoPoint pGeoPoint, boolean pSpeechBalloon, int pOffsetX, int pOffsetY) {
            mTitle = pTitle;
            mGeoPoint = pGeoPoint;
            mSpeechBalloon = pSpeechBalloon;
            mOffsetX = pOffsetX;
            mOffsetY = pOffsetY;
        }

        POI(String pTitle, GeoPoint pGeoPoint, int pOffsetX, int pOffsetY) {
            this(pTitle, pGeoPoint, true, pOffsetX, pOffsetY);
        }

        POI(String pTitle, GeoPoint pGeoPoint) {
            this(pTitle, pGeoPoint, false, 0, 0);
        }
    }
}

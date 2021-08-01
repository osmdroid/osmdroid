package org.osmdroid.views.overlay.gridlines;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.LinearRing;
import org.osmdroid.views.overlay.Overlay;

import java.text.DecimalFormat;

/**
 * created on 2/7/2018.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class LatLonGridlineOverlay2 extends Overlay {

    protected DecimalFormat mDecimalFormatter = new DecimalFormat("#.#####");
    //used to adjust the number of grid lines displayed on screen
    protected float mMultiplier = 1f;
    protected   Paint mLinePaint = new Paint();
    protected Paint mTextBackgroundPaint = new Paint();
    protected Paint mTextPaint = new Paint();
    protected  GeoPoint mOptimizationGeoPoint = new GeoPoint(0., 0);
    protected  Point mOptimizationPoint = new Point();

    public LatLonGridlineOverlay2() {
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mTextBackgroundPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setLineColor(Color.BLACK);
        setFontColor(Color.WHITE);
        setBackgroundColor(Color.BLACK);
        setLineWidth(1f);
        setFontSizeDp((short) 32);
    }

    @Override
    public void draw(Canvas c, Projection pProjection) {
        if (!isEnabled()) return;

        final double incrementor = getIncrementor((int) pProjection.getZoomLevel());
        final GeoPoint mapCenter = pProjection.getCurrentCenter();
        final double startLongitude = incrementor * Math.round(mapCenter.getLongitude() / incrementor);
        final double startLatitude = computeStartLatitude(mapCenter.getLatitude(), incrementor);
        final double worldMapSize = pProjection.getWorldMapSize();
        final float screenWidth = pProjection.getWidth();
        final float screenHeight = pProjection.getHeight();
        final float screenCenterX = screenWidth / 2;
        final float screenCenterY = screenHeight / 2;
        final float screenDiagonal = (float) Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight);
        final double screenRadius = screenDiagonal / 2;
        final double squaredScreenRadius = screenRadius * screenRadius;
        final float textOffsetX = screenWidth / 5;
        final float textOffsetY = screenHeight / 5;
        final float textBaseline = -mTextPaint.ascent() + 0.5f;
        final float textDescent = mTextPaint.descent() + 0.5f;
        final float textHeight = textBaseline + textDescent;

        for (int lineOrText = 0; lineOrText <= 1; lineOrText++) { // draw lines first, then texts
            for (int latOrLon = 0; latOrLon <= 1; latOrLon++) { // latitude then longitude lines
                final float orientation = -pProjection.getOrientation() + (latOrLon == 0 ? 0 : 90);
                for (int increaseOrDecrease = 0; increaseOrDecrease <= 1; increaseOrDecrease++) { // in both directions
                    final double delta = increaseOrDecrease == 0 ? incrementor : -incrementor;
                    int latest = latOrLon == 0 ? Math.round(screenCenterY) : Math.round(screenCenterX); // as close to the screen center as possible
                    boolean stillVisible = true;
                    double longitude = startLongitude;
                    double latitude = startLatitude;
                    for (int i = 0; stillVisible; i++) {
                        if (i > 0) {
                            if (latOrLon == 1) {
                                longitude += delta;
                                while (longitude < -180) {
                                    longitude += 360;
                                }
                                while (longitude > 180) {
                                    longitude -= 360;
                                }
                            } else {
                                latitude += delta;
                                if (latitude > MapView.getTileSystem().getMaxLatitude()) {
                                    latitude = computeStartLatitude(MapView.getTileSystem().getMinLatitude(), incrementor);
                                } else if (latitude < MapView.getTileSystem().getMinLatitude()) {
                                    latitude = computeStartLatitude(MapView.getTileSystem().getMaxLatitude(), incrementor);
                                }
                            }
                        }
                        mOptimizationGeoPoint.setCoords(latitude, longitude);
                        pProjection.toPixels(mOptimizationGeoPoint, mOptimizationPoint);
                        if (latOrLon == 0) {
                            mOptimizationPoint.y = (int) Math.round(LinearRing.getCloserValue(latest, mOptimizationPoint.y, worldMapSize));
                            // low zoom fix
                            if (i > 0) {
                                if (delta < 0) { // when decreasing the degrees, we should find increased Y
                                    while (mOptimizationPoint.y < latest) { // if not, let's add the world
                                        mOptimizationPoint.y += worldMapSize;
                                    }
                                } else {
                                    while (mOptimizationPoint.y > latest) {
                                        mOptimizationPoint.y -= worldMapSize;
                                    }
                                }
                            }
                            latest = mOptimizationPoint.y;
                        } else {
                            mOptimizationPoint.x = (int) Math.round(LinearRing.getCloserValue(latest, mOptimizationPoint.x, worldMapSize));
                            latest = mOptimizationPoint.x;
                        }
                        if (i == 0 && increaseOrDecrease == 1) { // special case: already done with i=0,increaseOrDecrease=0
                            continue;
                        }
                        final float xA;
                        final float yA;
                        final float xB;
                        final float yB;
                        final double squaredDistanceToCenter;
                        if (latOrLon == 0) {
                            yA = yB = mOptimizationPoint.y;
                            xA = screenCenterX - screenDiagonal;
                            xB = screenCenterX + screenDiagonal;
                            squaredDistanceToCenter = (mOptimizationPoint.y - screenCenterY) * (mOptimizationPoint.y - screenCenterY);
                        } else {
                            xA = xB = mOptimizationPoint.x;
                            yA = screenCenterY - screenDiagonal;
                            yB = screenCenterY + screenDiagonal;
                            squaredDistanceToCenter = (mOptimizationPoint.x - screenCenterX) * (mOptimizationPoint.x - screenCenterX);
                        }
                        stillVisible = squaredDistanceToCenter <= squaredScreenRadius;
                        if (stillVisible) {
                            if (lineOrText == 0) { // draw lines
                                c.drawLine(xA, yA, xB, yB, mLinePaint);
                            } else { // draw text
                                final String text = formatCoordinate(latOrLon == 0 ? latitude : longitude, latOrLon == 0);
                                float textCenterX = latOrLon == 0 ? textOffsetX : xA;
                                float textCenterY = latOrLon == 0 ? yA : screenHeight - textOffsetY;
                                final float textWidth = mTextPaint.measureText(text) + 0.5f;

                                if (orientation != 0) {
                                    c.save();
                                    c.rotate(orientation, textCenterX, textCenterY);
                                }
                                c.drawRect(textCenterX - textWidth / 2f, textCenterY - textHeight / 2f,
                                        textCenterX + textWidth / 2f, textCenterY + textHeight / 2f,
                                        mTextBackgroundPaint);
                                c.drawText(text, textCenterX, textCenterY + textHeight / 2 - textDescent, mTextPaint);
                                if (orientation != 0) {
                                    c.restore();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setDecimalFormatter(DecimalFormat df) {
        this.mDecimalFormatter = df;
    }

    public void setLineColor(int lineColor) {
        mLinePaint.setColor(lineColor);
    }

    public void setFontColor(int fontColor) {
        mTextPaint.setColor(fontColor);
    }

    public void setFontSizeDp(short fontSizeDp) {
        mTextPaint.setTextSize(fontSizeDp);
    }

    /**
     * sets the text label paint styler
     * see https://github.com/osmdroid/osmdroid/issues/1723
     * @since 6.1.11
     * @param paint
     */
    public void setTextStyle(Paint.Style paint) { mTextPaint.setStyle(paint);}

    /**
     * if for some reason there's missing setter for this class and you don't want to subclass it,
     * you can override the paint object with this method. Only used for the text painter
     * @since 6.1.11
     * @param paint
     */
    public void setTextPaint(Paint paint) { mTextPaint= paint;}

    /**
     * getter for the Paint object. I'd suggest using the setter methods first or subclassing this class
     * if you need to do something else but this will get you access to the live instance of the paint object
     * which is used for drawing text labels
     * @since 6.1.11
     * @return
     */
    public Paint getTextPaint() { return mTextPaint;}



    /**
     * background color for the text labels
     */
    public void setBackgroundColor(int backgroundColor) {
        mTextBackgroundPaint.setColor(backgroundColor);
    }

    public void setLineWidth(float lineWidth) {
        mLinePaint.setStrokeWidth(lineWidth);
    }

    /**
     * default is 1, larger number = more lines on screen. This comes at a performance penalty though
     */
    public void setMultiplier(float multiplier) {
        this.mMultiplier = multiplier;
    }

    /**
     * this gets the distance in decimal degrees in between each line on the grid based on zoom level.
     * i had had it at more logical increments (90, 45, 30, etc) but changing to factors of 90 helps visualization
     * (i.e. when you zoom in on a particular crosshair, the crosshair is still there at the next zoom level, for the most part
     *
     * @param zoom mapview's osm zoom level
     * @return a double indicating the distance in degrees/decimal from which to place the gridlines on screen
     */
    protected double getIncrementor(int zoom) {

        switch (zoom) {
            case 0:
            case 1:
                return 30d * mMultiplier;
            case 2:
                return 15d * mMultiplier;
            case 3:
                return 9d * mMultiplier;
            case 4:
                return 6d * mMultiplier;
            case 5:
                return 3d * mMultiplier;
            case 6:
                return 2d * mMultiplier;
            case 7:
                return 1d * mMultiplier;
            case 8:
                return 0.5d * mMultiplier;
            case 9:
                return 0.25d * mMultiplier;
            case 10:
                return 0.1d * mMultiplier;
            case 11:
                return 0.05d * mMultiplier;
            case 12:
                return 0.025d * mMultiplier;
            case 13:
                return 0.0125d * mMultiplier;
            case 14:
                return 0.00625d * mMultiplier;
            case 15:
                return 0.003125d * mMultiplier;
            case 16:
                return 0.0015625 * mMultiplier;
            case 17:
                return 0.00078125 * mMultiplier;
            case 18:
                return 0.000390625 * mMultiplier;
            case 19:
                return 0.0001953125 * mMultiplier;
            case 20:
                return 0.00009765625 * mMultiplier;
            case 21:
                return 0.000048828125 * mMultiplier;
            case 22:
                return 0.0000244140625 * mMultiplier;
            case 23:
                return 0.00001220703125 * mMultiplier;
            case 24:
                return 0.000006103515625 * mMultiplier;
            case 25:
                return 0.0000030517578125 * mMultiplier;
            case 26:
                return 0.00000152587890625 * mMultiplier;
            case 27:
                return 0.000000762939453125 * mMultiplier;
            case 28:
                return 0.0000003814697265625 * mMultiplier;
            case 29:
            default:
                return 0.00000019073486328125 * mMultiplier;
        }
    }

    /**
     * @since 6.1.7
     * Computes the start latitude when dealing with a latitude and an incrementor
     * Special focus on the "beyond possible" values of latitudes
     */
    private double computeStartLatitude(final double pLatitude, final double pIncrementor) {
        double result = pIncrementor * Math.round(pLatitude / pIncrementor);
        while (result > MapView.getTileSystem().getMaxLatitude()) {
            result -= pIncrementor;
        }
        while (result < MapView.getTileSystem().getMinLatitude()) {
            result += pIncrementor;
        }
        return result;
    }

    /**
     * @since 6.1.7
     */
    private String formatCoordinate(final double pValue, final boolean pLatitudeOrLongitude) {
        return mDecimalFormatter.format(pValue) + (pValue == 0 ? "" : pValue > 0 ?
                (pLatitudeOrLongitude ? "N" : "E") : (pLatitudeOrLongitude ? "S" : "W"));
    }
}

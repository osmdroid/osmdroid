package org.osmdroid.samplefragments.data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.milestones.MilestoneBitmapDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneLineDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneLister;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceLister;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceSliceLister;
import org.osmdroid.views.overlay.milestones.MilestonePathDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneVertexLister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabrice on 28/12/2017.
 *
 * @since 6.0.0
 */

public class SampleRace extends BaseSampleFragment {

    public static final String TITLE = "10K race in Paris";

    private static final float LINE_WIDTH_BIG = 12;
    private static final float TEXT_SIZE = 20;
    private static final int COLOR_POLYLINE_STATIC = Color.BLUE;
    private static final int COLOR_POLYLINE_ANIMATED = Color.GREEN;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    private double mAnimatedMetersSoFar;
    private boolean mAnimationEnded;

    /**
     * @since 6.0.3
     */
    private final List<GeoPoint> mGeoPoints = getGeoPoints();

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                final BoundingBox boundingBox = BoundingBox.fromGeoPoints(mGeoPoints);
                mMapView.zoomToBoundingBox(boundingBox, false, 30);
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        final Polyline line = new Polyline(mMapView);
        line.getOutlinePaint().setColor(COLOR_POLYLINE_STATIC);
        line.getOutlinePaint().setStrokeWidth(LINE_WIDTH_BIG);
        line.setPoints(mGeoPoints);
        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
        final List<MilestoneManager> managers = new ArrayList<>();
        final MilestoneMeterDistanceSliceLister slicerForPath = new MilestoneMeterDistanceSliceLister();
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), org.osmdroid.library.R.drawable.next);
        final MilestoneMeterDistanceSliceLister slicerForIcon = new MilestoneMeterDistanceSliceLister();
        managers.add(getAnimatedPathManager(slicerForPath));
        managers.add(getAnimatedIconManager(slicerForIcon, bitmap));
        managers.add(getHalfKilometerManager());
        managers.add(getKilometerManager());
        managers.add(getStartManager(bitmap));
        line.setMilestoneManagers(managers);
        mMapView.getOverlayManager().add(line);
        final ValueAnimator percentageCompletion = ValueAnimator.ofFloat(0, 10000); // 10 kilometers
        percentageCompletion.setDuration(5000); // 5 seconds
        percentageCompletion.setStartDelay(1000); // 1 second
        percentageCompletion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedMetersSoFar = (float) animation.getAnimatedValue();
                slicerForPath.setMeterDistanceSlice(0, mAnimatedMetersSoFar);
                slicerForIcon.setMeterDistanceSlice(mAnimatedMetersSoFar, mAnimatedMetersSoFar);
                mMapView.invalidate();
            }
        });
        percentageCompletion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationEnded = true;
            }
        });
        percentageCompletion.start();
    }

    /**
     * @since 6.0.2
     */
    private Paint getFillPaint(final int pColor) {
        final Paint paint = new Paint();
        paint.setColor(pColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        return paint;
    }

    /**
     * @since 6.0.2
     */
    private Paint getStrokePaint(final int pColor, final float pWidth) {
        final Paint paint = new Paint();
        paint.setStrokeWidth(pWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(pColor);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    /**
     * @since 6.0.2
     */
    private Paint getTextPaint(final int pColor) {
        final Paint paint = new Paint();
        paint.setColor(pColor);
        paint.setTextSize(TEXT_SIZE);
        paint.setAntiAlias(true);
        return paint;
    }

    /**
     * Kilometer milestones
     *
     * @since 6.0.2
     */
    private MilestoneManager getKilometerManager() {
        final float backgroundRadius = 20;
        final Paint backgroundPaint1 = getFillPaint(COLOR_BACKGROUND);
        final Paint backgroundPaint2 = getFillPaint(COLOR_POLYLINE_ANIMATED);
        final Paint textPaint1 = getTextPaint(COLOR_POLYLINE_STATIC);
        final Paint textPaint2 = getTextPaint(COLOR_BACKGROUND);
        final Paint borderPaint = getStrokePaint(COLOR_BACKGROUND, 2);
        return new MilestoneManager(
                new MilestoneMeterDistanceLister(1000),
                new MilestoneDisplayer(0, false) {
                    @Override
                    protected void draw(final Canvas pCanvas, final Object pParameter) {
                        final double meters = (double) pParameter;
                        final int kilometers = (int) Math.round(meters / 1000);
                        final boolean checked = meters < mAnimatedMetersSoFar || (kilometers == 10 && mAnimationEnded);
                        final Paint textPaint = checked ? textPaint2 : textPaint1;
                        final Paint backgroundPaint = checked ? backgroundPaint2 : backgroundPaint1;
                        final String text = "" + kilometers + "K";
                        final Rect rect = new Rect();
                        textPaint1.getTextBounds(text, 0, text.length(), rect);
                        pCanvas.drawCircle(0, 0, backgroundRadius, backgroundPaint);
                        pCanvas.drawText(text, -rect.left - rect.width() / 2, rect.height() / 2 - rect.bottom, textPaint);
                        pCanvas.drawCircle(0, 0, backgroundRadius + 1, borderPaint);
                    }
                }
        );
    }

    /**
     * Half-kilometer milestones
     *
     * @since 6.0.2
     */
    private MilestoneManager getHalfKilometerManager() {
        final Path arrowPath = new Path(); // a simple arrow towards the right
        arrowPath.moveTo(-5, -5);
        arrowPath.lineTo(5, 0);
        arrowPath.lineTo(-5, 5);
        arrowPath.close();
        final Paint backgroundPaint = getFillPaint(COLOR_BACKGROUND);
        return new MilestoneManager( // display an arrow at 500m every 1km
                new MilestoneMeterDistanceLister(500),
                new MilestonePathDisplayer(0, true, arrowPath, backgroundPaint) {
                    @Override
                    protected void draw(final Canvas pCanvas, final Object pParameter) {
                        final int halfKilometers = (int) Math.round(((double) pParameter / 500));
                        if (halfKilometers % 2 == 0) {
                            return;
                        }
                        super.draw(pCanvas, pParameter);
                    }
                }
        );
    }

    /**
     * Animated path
     *
     * @since 6.0.2
     */
    private MilestoneManager getAnimatedPathManager(final MilestoneLister pMilestoneLister) {
        final Paint slicePaint = getStrokePaint(COLOR_POLYLINE_ANIMATED, LINE_WIDTH_BIG);
        return new MilestoneManager(pMilestoneLister, new MilestoneLineDisplayer(slicePaint));
    }

    /**
     * Animated icon
     *
     * @since 6.0.2
     */
    private MilestoneManager getAnimatedIconManager(final MilestoneLister pMilestoneLister,
                                                    final Bitmap pBitmap) {
        return new MilestoneManager(
                pMilestoneLister,
                new MilestoneBitmapDisplayer(0, true, pBitmap,
                        pBitmap.getWidth() / 2, pBitmap.getHeight() / 2)
        );
    }

    /**
     * Starting point
     *
     * @since 6.0.2
     */
    private MilestoneManager getStartManager(final Bitmap pBitmap) {
        return new MilestoneManager(
                new MilestoneVertexLister(),
                new MilestoneBitmapDisplayer(0, true,
                        pBitmap, pBitmap.getWidth() / 2, pBitmap.getHeight() / 2) {
                    @Override
                    protected void draw(final Canvas pCanvas, final Object pParameter) {
                        if (0 != (int) pParameter) { // we only draw the start
                            return;
                        }
                        super.draw(pCanvas, pParameter);
                    }
                }
        );
    }

    /**
     * @since 6.0.2
     * TODO get a real list of geo points instead of this lousy manual list
     */
    private List<GeoPoint> getGeoPoints() {
        final List<GeoPoint> pts = new ArrayList<>();
        pts.add(new GeoPoint(48.85546563875735, 2.359844067173981)); // saint paul
        pts.add(new GeoPoint(48.85737826660179, 2.351524365470226)); // hôtel de ville
        pts.add(new GeoPoint(48.86253652215784, 2.3354870181106264)); // louvre 1
        pts.add(new GeoPoint(48.86292409137066, 2.3356209116511195)); // louvre 2
        pts.add(new GeoPoint(48.86989982398147, 2.332474413449688)); // opéra loop 1
        pts.add(new GeoPoint(48.87019045840439, 2.3327154218225985)); // opéra loop 2
        pts.add(new GeoPoint(48.87100070303335, 2.332420856033508)); // opéra loop 3
        pts.add(new GeoPoint(48.871987070089496, 2.3330367663197364)); // opéra loop 4
        pts.add(new GeoPoint(48.87285012531207, 2.3319923967039813)); // opéra loop 5
        pts.add(new GeoPoint(48.87270041271832, 2.33134970770962)); // opéra loop 6
        pts.add(new GeoPoint(48.87166121883793, 2.330720408069368)); // opéra loop 7
        pts.add(new GeoPoint(48.87096547527885, 2.331885281871564)); // opéra loop 8
        pts.add(new GeoPoint(48.87003193074662, 2.3321932370146783)); // opéra loop 9
        pts.add(new GeoPoint(48.86989982398147, 2.332474413449688)); // opéra loop 10
        pts.add(new GeoPoint(48.864306984328245, 2.3350719481351234)); // rue de l'échelle 1
        pts.add(new GeoPoint(48.86316191644713, 2.3338401275626666)); // rue de l'échelle 2
        pts.add(new GeoPoint(48.866209500723855, 2.3235169355912433)); // rivoli
        pts.add(new GeoPoint(48.866729156977776, 2.3223118937268623)); // concorde
        pts.add(new GeoPoint(48.86901910330005, 2.3239721736289027)); // madeleine loop 1
        pts.add(new GeoPoint(48.8691952486765, 2.3249897645366104)); // madeleine loop 2
        pts.add(new GeoPoint(48.87022568670458, 2.325927019319977)); // madeleine loop 3
        pts.add(new GeoPoint(48.870489898165346, 2.32583329384164)); // madeleine loop 4
        pts.add(new GeoPoint(48.87073649426996, 2.3250165432446863)); // madeleine loop 5
        pts.add(new GeoPoint(48.87075410823092, 2.3247085881016005)); // madeleine loop 6
        pts.add(new GeoPoint(48.86957395913612, 2.323570493007452)); // madeleine loop 7
        pts.add(new GeoPoint(48.86901910330005, 2.3239721736289027)); // madeleine loop 8
        pts.add(new GeoPoint(48.86664988772853, 2.3224457872673554)); // concorde 1
        pts.add(new GeoPoint(48.866183077380335, 2.3231420336778967)); // concorde 2
        pts.add(new GeoPoint(48.865610568177935, 2.3231688123859726)); // concorde 3
        pts.add(new GeoPoint(48.86398108306007, 2.321307692173235)); // concorde 4
        pts.add(new GeoPoint(48.863531864319754, 2.3216022579623257)); // concorde 5
        pts.add(new GeoPoint(48.86047157217769, 2.3306186871927252)); // pont césaire
        pts.add(new GeoPoint(48.859105908108276, 2.336824405441064)); // mitterrand 1
        pts.add(new GeoPoint(48.858679130445125, 2.3402407938844476)); // mitterrand 2
        pts.add(new GeoPoint(48.85792514768071, 2.342640914879439)); // pont neuf
        pts.add(new GeoPoint(48.8563361600739, 2.3489338967683864)); // pont notre dame
        pts.add(new GeoPoint(48.85582206974299, 2.3509713700276507)); // pont d'arcole
        pts.add(new GeoPoint(48.85403498622509, 2.3547049339593116)); // pont louis philippe
        pts.add(new GeoPoint(48.85303073607055, 2.3575358780393856)); // pont marie
        pts.add(new GeoPoint(48.852894107137885, 2.358500835434853)); // quai des célestins 1
        pts.add(new GeoPoint(48.85275705072659, 2.3589590819111095)); // quai des célestins 2
        pts.add(new GeoPoint(48.852639573503986, 2.3594411333991445)); // quai des célestins 3
        pts.add(new GeoPoint(48.85244769344759, 2.3598755748636506)); // quai des célestins 4
        pts.add(new GeoPoint(48.85215399805951, 2.360375480110463)); // quai des célestins 5
        return pts;
    }
}

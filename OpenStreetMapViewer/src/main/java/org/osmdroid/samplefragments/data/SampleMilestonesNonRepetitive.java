package org.osmdroid.samplefragments.data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.osmdroid.R;
import org.osmdroid.data.DataCountry;
import org.osmdroid.data.DataCountryLoader;
import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.milestones.MilestoneDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneLineDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneLister;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceLister;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceSliceLister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Demo with the new "non repetitive milestones" feature - a map of all capitals of the EU
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */
public class SampleMilestonesNonRepetitive extends SampleMapEventListener {

    private double mAnimatedMetersSoFar;
    private boolean mAnimationEnded;
    private final String[] mOrder = new String[]{ // arbitrary order
            "FRA", "LUX", "BEL", "NLD",
            "GBR", "IRL",
            "PRT", "ESP",
            "MLT", "ITA", "HRV", "SVN",
            "DEU", "DNK", "SWE", "FIN",
            "EST", "LVA", "LTU", "POL", "CZE", "AUT", "SVK", "HUN",
            "ROU", "BGR", "GRC", "CYP"
    };

    // source https://en.wikipedia.org/wiki/Flag_of_Europe#Colours
    private final int COLOR_BLUE = Color.rgb(0, 51, 153);
    private final int COLOR_GOLD = Color.rgb(255, 204, 0);
    private final int mLineWidth = 6;
    private final int mDiskRadius = 18;

    @Override
    public String getSampleTitle() {
        return "Milestones with non repetitive values";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();

        final LinkedHashMap<String, DataCountry> mList;
        try {
            mList = new DataCountryLoader(getActivity(), R.raw.data_country).getList();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        final Polyline polyline = new Polyline();
        final List<GeoPoint> capitals = new ArrayList<>(mOrder.length);
        final double distances[] = new double[mOrder.length];
        int distancesIndex = 0;
        double distance1 = 0;
        GeoPoint previous = null;
        for (final String country : mOrder) {
            final GeoPoint capital = new GeoPoint(mList.get(country).getCapitalGeoPoint());
            if (distancesIndex == 0) {
                distance1 = 0;
            } else {
                distance1 += previous.distanceToAsDouble(capital);
            }
            distances[distancesIndex++] = distance1;
            previous = new GeoPoint(capital);
            capitals.add(capital);
        }
        final BoundingBox boundingBox = BoundingBox.fromGeoPoints(capitals);
        polyline.setPoints(capitals);
        polyline.getOutlinePaint().setColor(Color.TRANSPARENT);
        final List<MilestoneManager> managers = new ArrayList<>();
        final MilestoneMeterDistanceSliceLister slicerForPath = new MilestoneMeterDistanceSliceLister();
        managers.add(getAnimatedPathManager(slicerForPath));

        final Paint backgroundPaint = getFillPaint(COLOR_BLUE);
        final Paint starPaint = getFillPaint(COLOR_GOLD);
        managers.add(new MilestoneManager(
                new MilestoneMeterDistanceLister(distances),
                new MilestoneDisplayer(0, false) {
                    private final Path mPath = new Path();

                    @Override
                    protected void draw(final Canvas pCanvas, final Object pParameter) {
                        final double meters = (double) pParameter;
                        final boolean checked = meters < mAnimatedMetersSoFar || mAnimationEnded;
                        if (!checked) {
                            return;
                        }

                        pCanvas.drawCircle(0, 0, mDiskRadius, backgroundPaint);

                        // drawing a star
                        // inspired by https://stackoverflow.com/questions/7007429/android-how-to-draw-triangle-star-square-heart-on-the-canvas
                        mPath.reset();
                        // top left
                        mPath.moveTo(mDiskRadius * -.5f, mDiskRadius * -.16f);
                        // top right
                        mPath.lineTo(mDiskRadius * .5f, mDiskRadius * -.16f);
                        // bottom left
                        mPath.lineTo(mDiskRadius * -.32f, mDiskRadius * .45f);
                        // top tip
                        mPath.lineTo(0, mDiskRadius * -.5f);
                        // bottom right
                        mPath.lineTo(mDiskRadius * .32f, mDiskRadius * .45f);
                        mPath.close();
                        pCanvas.drawPath(mPath, starPaint);
                    }
                }
        ));

        polyline.setMilestoneManagers(managers);

        mMapView.getOverlayManager().add(polyline);
        final float distance = (float) polyline.getDistance();
        final float fraction = 1f / 10; // fraction of the polyline to be displayed
        final ValueAnimator percentageCompletion = ValueAnimator.ofFloat(0, distance);
        percentageCompletion.setDuration(5000); // 5 seconds
        percentageCompletion.setStartDelay(500); // .5 second
        percentageCompletion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedMetersSoFar = (float) animation.getAnimatedValue();
                if (mAnimatedMetersSoFar < distance * fraction) {
                    slicerForPath.setMeterDistanceSlice(0, mAnimatedMetersSoFar);
                } else if (mAnimatedMetersSoFar > distance * (1 - fraction)) {
                    slicerForPath.setMeterDistanceSlice(mAnimatedMetersSoFar - (distance - mAnimatedMetersSoFar), mAnimatedMetersSoFar);
                } else {
                    slicerForPath.setMeterDistanceSlice(mAnimatedMetersSoFar - distance * fraction, mAnimatedMetersSoFar);
                }
                mMapView.invalidate();
            }
        });
        percentageCompletion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationEnded = true;
                mMapView.invalidate();
            }
        });
        percentageCompletion.start();

        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(boundingBox, false, 30);
            }
        });
    }

    private MilestoneManager getAnimatedPathManager(final MilestoneLister pMilestoneLister) {
        final Paint paint = new Paint();
        paint.setStrokeWidth(mLineWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(COLOR_GOLD);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return new MilestoneManager(pMilestoneLister, new MilestoneLineDisplayer(paint));
    }

    private Paint getFillPaint(final int pColor) {
        final Paint paint = new Paint();
        paint.setColor(pColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        return paint;
    }
}

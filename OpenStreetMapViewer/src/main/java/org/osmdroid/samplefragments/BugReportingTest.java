package org.osmdroid.samplefragments;

/**
 * Created by alex on 7/9/16.
 */

public class BugReportingTest extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Crash reporting";
    }

    @Override
    public void addOverlays()  {
        super.addOverlays();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new IllegalArgumentException("This is a test of automated bug reporting");
            }
        }).start();


    }
}

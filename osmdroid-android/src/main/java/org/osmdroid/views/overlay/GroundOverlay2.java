package org.osmdroid.views.overlay;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;

/**
 * A ground overlay is an image that is fixed to 2 corners on a map using simple scaling
 * that does not take into consideration the curvature of the Earth.
 * @since 6.0.0
 * @author pasniak inspired by zkhan's code in Avare
 *
 */
public class GroundOverlay2 extends AbstractGroundOverlay {

    private float   mLonL,
                    mLatU,
                    mLonR,
                    mLatD;

	public GroundOverlay2() {
		super();
	}

	/**
	 * @param UL upper left
	 * @param RD lower right
	 */
	public void setPosition(GeoPoint UL, GeoPoint RD)
    {
        mLatU = (float)UL.getLatitude();
        mLonL = (float)UL.getLongitude();
        mLatD = (float)RD.getLatitude();
        mLonR = (float)RD.getLongitude();
	}

	@Override
	protected void computeMatrix(final Projection pProjection) {
		long x0 = pProjection.getLongPixelXFromLongitude(mLonL),
				y0 = pProjection.getLongPixelYFromLatitude(mLatU),
				x1 = pProjection.getLongPixelXFromLongitude(mLonR),
				y1 = pProjection.getLongPixelYFromLatitude(mLatD);

		float widthOnTheMap = x1 - x0,
				heightOnTheMap = y1 - y0;

		float scaleX = widthOnTheMap / getImage().getWidth(),
				scaleY = heightOnTheMap / getImage().getHeight();

		getMatrix().setScale(scaleX, scaleY);
		getMatrix().postTranslate(x0, y0);
	}
}

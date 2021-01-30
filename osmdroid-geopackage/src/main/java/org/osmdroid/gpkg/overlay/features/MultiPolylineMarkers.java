/**
 * The MIT License
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * This code was sourced from the National Geospatial Intelligency Agency and was
 * originally licensed under the MIT license. It has been modified to support
 * osmdroid's APIs.
 * <p>
 * You can find the original code base here:
 * https://github.com/ngageoint/geopackage-android-map
 * https://github.com/ngageoint/geopackage-android
 */
package org.osmdroid.gpkg.overlay.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polyline Markers object
 *
 * @author osbornb
 */
public class MultiPolylineMarkers {

    private List<PolylineMarkers> polylineMarkers = new ArrayList<PolylineMarkers>();

    public void add(PolylineMarkers polylineMarker) {
        polylineMarkers.add(polylineMarker);
    }

    public List<PolylineMarkers> getPolylineMarkers() {
        return polylineMarkers;
    }

    public void setPolylineMarkers(List<PolylineMarkers> polylineMarkers) {
        this.polylineMarkers = polylineMarkers;
    }

    /**
     * Update based upon marker changes

     public void update() {
     for (PolylineMarkers polylineMarker : polylineMarkers) {
     polylineMarker.update();
     }
     }  */

    /**
     * Remove the polyline and points

     public void remove() {
     for (PolylineMarkers polylineMarker : polylineMarkers) {
     polylineMarker.remove();
     }
     }   */

    /**
     * Set visibility on the map
     *
     * @param visible visibility flag
     * @since 1.3.2
     */
    public void setVisible(boolean visible) {
        for (PolylineMarkers polylineMarker : polylineMarkers) {
            polylineMarker.setVisible(visible);
        }
    }

    /**
     * Is it valid
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = true;
        for (PolylineMarkers polyline : polylineMarkers) {
            valid = polyline.isValid();
            if (!valid) {
                break;
            }
        }
        return valid;
    }

    /**
     * Is it deleted
     *
     * @return
     */
    public boolean isDeleted() {
        boolean deleted = true;
        for (PolylineMarkers polyline : polylineMarkers) {
            deleted = polyline.isDeleted();
            if (!deleted) {
                break;
            }
        }
        return deleted;
    }

}

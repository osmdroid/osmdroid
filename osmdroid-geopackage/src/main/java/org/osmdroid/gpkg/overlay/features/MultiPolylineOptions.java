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

import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;


/**
 * Multiple Polyline Options object
 *
 * @author osbornb
 */
public class MultiPolylineOptions {

    private List<Polyline> polylineOptions = new ArrayList<Polyline>();

    private PolylineOptions options;

    public void add(Polyline polylineOption) {
        polylineOptions.add(polylineOption);
    }

    public List<Polyline> getPolylineOptions() {
        return polylineOptions;
    }

    public PolylineOptions getOptions() {
        return options;
    }

    public void setOptions(PolylineOptions options) {
        this.options = options;
    }

    public void setPolylineOptions(List<Polyline> polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

}

package org.osmdroid.wms;

import org.osmdroid.wms.v111.LatLonBoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data model for WMS layers
 * 1/10/16.
 * @author Alex O'Ree
 * @since 5.6.6
 */
public class WMSLayer {
    private String name;
    private String title;
    //maps to 'abstract' wms element
    private String description;
    private LatLonBoundingBox bbox;
    private String srs;

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    private List<String> styles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLonBoundingBox getBbox() {
        return bbox;
    }

    public void setBbox(LatLonBoundingBox bbox) {
        this.bbox = bbox;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }
}

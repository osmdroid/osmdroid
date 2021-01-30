package org.osmdroid.wms;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data model for WMS layers
 * 1/10/16.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */
public class WMSLayer {
    private int pixelSize = 256;
    /**
     * the name goes in the url and is machine intrepretable
     */
    private String name;
    /**
     * human readable title
     */
    private String title;
    //maps to 'abstract' wms element
    private String description;
    //TODO replace with osmdroid boundingbox
    private BoundingBox bbox;
    private List<String> srs = new ArrayList<>();

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

    public BoundingBox getBbox() {
        return bbox;
    }

    public void setBbox(BoundingBox bbox) {
        this.bbox = bbox;
    }


    public int getPixelSize() {
        return pixelSize;
    }

    public void setPixelSize(int pixelSize) {
        this.pixelSize = pixelSize;
    }

    public List<String> getSrs() {
        return srs;
    }

}

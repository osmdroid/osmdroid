package org.osmdroid.wms;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple data model for represeting a WMS server
 * https://github.com/osmdroid/osmdroid/issues/177
 *
 * @author Alex O'Ree
 * 1/10/16.
 * @since 6.0.0
 */
public class WMSEndpoint {
    private String name, description, title;
    private String wmsVersion = "1.1.0";
    //capability/getmap/HTTP/Get/OnlineResource
    private String baseurl;
    private List<WMSLayer> layers = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getWmsVersion() {
        return wmsVersion;
    }

    public void setWmsVersion(String wmsVersion) {
        this.wmsVersion = wmsVersion;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    public List<WMSLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<WMSLayer> layers) {
        this.layers = layers;
    }
}

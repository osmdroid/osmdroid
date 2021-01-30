package org.osmdroid.wms;

import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.TileSystem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides parsing for WMS 1.1.1 and 1.3.0. The schema's are close enough that the
 * parsing we do will work with both cases.
 * This is primarily for internal use only. See WMSParser
 * https://github.com/osmdroid/osmdroid/issues/177
 * created on 8/25/2017.
 *
 * @author Alex O'Ree
 * @see WMSLayer
 * @see WMSParser
 * @see WMSEndpoint
 * @since 6.0.0
 */

public class DomParserWms111 {
    static final String TAG = "osmdroidwms";

    public static WMSEndpoint parse(Element element) throws Exception {
        //  WMTMSCapabilities ret = new WMTMSCapabilities();


        WMSEndpoint rets = new WMSEndpoint();
        rets.setWmsVersion(element.getAttribute("version"));

        //check the version attribute
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            if (e.getNodeName().contains("Service")) {
                extractService(e, rets);
//                ret.setService(parseService(e));

            } else if (e.getNodeName().contains("Capability")) {
                extractCapability(e, rets);
                //           ret.setCapability(parseCapability(e));

            }
        }
        List<WMSLayer> deleteme = new ArrayList<>();
        for (int i = 0; i < rets.getLayers().size(); i++) {
            if (rets.getLayers().get(i).getName() == null)
                deleteme.add(rets.getLayers().get(i));
            else {
                if (rets.getLayers().get(i).getTitle() == null)
                    rets.getLayers().get(i).setTitle(rets.getLayers().get(i).getName());
            }
        }
        rets.getLayers().removeAll(deleteme);


        return rets;
    }

    private static WMSEndpoint extractCapability(Node element, WMSEndpoint rets) {


        //   Capability ret = new Capability();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            //System.out.println("parseCapabilties/" + name);
            // Starts by looking for the entry tag
            if (name.contains("Request")) {

                parseRequest(e, rets);

            } else if (name.contains("Exception")) {


            } else if (name.contains("Layer")) {

                rets.getLayers().addAll(parseLayers(e));

                //TODO
            } else {

            }
        }
        return rets;

    }

    private static void parseRequest(Node element, WMSEndpoint rets) {

        //   Capability ret = new Capability();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            //System.out.println("parseCapabilties/" + name);
            // Starts by looking for the entry tag
            if (name.contains("GetCapabilities")) {
                for (int i2 = 0; i2 < e.getChildNodes().getLength(); i2++) {
                    Node e3 = e.getChildNodes().item(i2);
                    String name3 = e3.getNodeName();

                    if (name3.contains("DCPType")) {
                        for (int i4 = 0; i4 < e3.getChildNodes().getLength(); i4++) {
                            Node e4 = e3.getChildNodes().item(i4);
                            String name4 = e4.getNodeName();
                            if (name4.contains("HTTP")) {
                                for (int i5 = 0; i5 < e4.getChildNodes().getLength(); i5++) {
                                    Node e5 = e4.getChildNodes().item(i5);
                                    String name5 = e5.getNodeName();
                                    if (name5.contains("Get")) {
                                        for (int i6 = 0; i6 < e5.getChildNodes().getLength(); i6++) {
                                            Node e6 = e5.getChildNodes().item(i6);
                                            String name6 = e6.getNodeName();
                                            if (name6.contains("OnlineResource")) {
                                                Node href = e6.getAttributes().getNamedItem("href");
                                                Node href2 = e6.getAttributes().getNamedItem("xlink:href");
                                                Node href3 = e6.getAttributes().getNamedItemNS("http://www.w3.org/1999/xlink", "href");

                                                if (href != null) {
                                                    rets.setBaseurl(href.getNodeValue());
                                                } else if (href2 != null) {
                                                    rets.setBaseurl(href2.getNodeValue());
                                                } else if (href3 != null) {
                                                    rets.setBaseurl(href3.getNodeValue());
                                                }

                                            }
                                        }

                                    }
                                }
                            }
                        }


                    }
                }
                //find format
                //find http
                //find get
                //find OnlineResource/href
            } else {

            }
        }

    }

    //e is "Layer"
    private static Collection<? extends WMSLayer> parseLayers(Node element) {
        final TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
        List<WMSLayer> rets = new ArrayList<>();
        WMSLayer ret = new WMSLayer();

        Double north = null;
        Double south = null;
        Double east = null;
        Double west = null;
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            if (name.contains("Name")) {
                ret.setName(e.getTextContent());
            } else if (name.contains("Title")) {
                ret.setTitle(e.getTextContent());
            } else if (name.contains("Abstract")) {
                ret.setDescription(e.getTextContent());
            } else if (name.contains("SRS")) {
                ret.getSrs().add(e.getTextContent());
            } else if (name.contains("CRS")) {
                ret.getSrs().add(e.getTextContent());
            } else if (name.contains("LatLonBoundingBox")) {
                //TODO need some handling for crs here
                south = (Double.parseDouble(e.getAttributes().getNamedItem("miny").getNodeValue()));
                if (south < tileSystem.getMinLatitude())
                    south = tileSystem.getMinLatitude();
                north = (Double.parseDouble(e.getAttributes().getNamedItem("maxy").getNodeValue()));

                if (north > tileSystem.getMaxLatitude())
                    north = tileSystem.getMaxLatitude();
                west = (Double.parseDouble(e.getAttributes().getNamedItem("maxx").getNodeValue()));
                east = (Double.parseDouble(e.getAttributes().getNamedItem("minx").getNodeValue()));
                ret.setBbox(new BoundingBox(north, east, south, west));

            } else if (name.contains("BoundingBox") && ret.getBbox() == null) {
                //need to check CRS first if it's valid
                //<BoundingBox CRS="CRS:84" minx="-179.999996" miny="-89.000000" maxx="179.999996" maxy="89.000000"/>
                //coordinates reversed?
                //<BoundingBox CRS="EPSG:4326" minx="-89.000000" miny="-179.999996" maxx="89.000000" maxy="179.999996"/>
                Node crs = e.getAttributes().getNamedItem("CRS");
                if (crs != null && crs.getAttributes() != null) {
                    Node maxx = crs.getAttributes().getNamedItem("maxx");
                    Node maxy = crs.getAttributes().getNamedItem("maxy");
                    Node miny = crs.getAttributes().getNamedItem("miny");
                    Node minx = crs.getAttributes().getNamedItem("minx");

                    boolean ok = maxx != null && maxy != null && minx != null && miny != null;
                    if (ok) {
                        if ("EPSG:4326".equals(crs.getNodeValue())) {
                            south = (Double.parseDouble(minx.getNodeValue()));
                            north = (Double.parseDouble(maxx.getNodeValue()));
                            west = (Double.parseDouble(maxy.getNodeValue()));
                            east = (Double.parseDouble(miny.getNodeValue()));
                            ret.setBbox(new BoundingBox(north, east, south, west));

                        } else if ("CRS:84".equals(crs.getNodeValue())) {

                            south = (Double.parseDouble(miny.getNodeValue()));
                            north = (Double.parseDouble(maxy.getNodeValue()));
                            west = (Double.parseDouble(maxx.getNodeValue()));
                            east = (Double.parseDouble(minx.getNodeValue()));
                            ret.setBbox(new BoundingBox(north, east, south, west));
                        } else {
                            Log.w(TAG, "warn, unhandled CRS/SRS " + crs.getNodeValue());
                        }
                    }
                }
            } else if (name.contains("Style")) {
                for (int k = 0; k < e.getChildNodes().getLength(); k++) {
                    Node e2 = e.getChildNodes().item(k);
                    if ("Name".equals(e2.getNodeName())) {
                        ret.getStyles().add(e2.getTextContent());
                    }
                }


            } else if (name.contains("Layer")) {
                rets.addAll(parseLayers(e));

            } else {

            }
        }

        Node pixelx = element.getAttributes().getNamedItem("fixedHeight");
        Node pixely = element.getAttributes().getNamedItem("fixedWidth");
        if (pixely != null && pixelx != null) {
            if (pixelx.getNodeValue().equals(pixely.getNodeValue())) {
                ret.setPixelSize(Integer.parseInt(pixelx.getNodeValue()));
            } else {
                Log.w(TAG, "Layer excluded due to non-equal height,width tile sizes");
                return rets;
            }
        }   //else the image size wasn't defined,

        rets.add(ret);
        return rets;
    }


    private static WMSEndpoint extractService(Node element, WMSEndpoint ret) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            // Starts by looking for the entry tag
            if (name.contains("Name")) {
                ret.setName(e.getTextContent());
            } else if (name.contains("Title")) {
                ret.setTitle(e.getTextContent());
            } else if (name.contains("Abstract")) {
                ret.setDescription(e.getTextContent());
            } else if (name.contains("OnlineResource")) {
                Node namedItem = e.getAttributes().getNamedItem("xlink:href");
                Node namedItem2 = e.getAttributes().getNamedItem("href");
                String baseUrl = null;
                if (namedItem != null)
                    baseUrl = namedItem.getNodeValue();
                if (namedItem2 != null)
                    baseUrl = namedItem2.getNodeValue();
                if (baseUrl != null) {
                    ret.setBaseurl(baseUrl);
                }

            }
        }
        return ret;
    }
}

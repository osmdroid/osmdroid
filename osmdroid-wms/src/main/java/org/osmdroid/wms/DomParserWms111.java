package org.osmdroid.wms;

import org.osmdroid.wms.v111.Capability;
import org.osmdroid.wms.v111.DCPType;
import org.osmdroid.wms.v111.Format;
import org.osmdroid.wms.v111.Get;
import org.osmdroid.wms.v111.GetCapabilities;
import org.osmdroid.wms.v111.HTTP;
import org.osmdroid.wms.v111.LatLonBoundingBox;
import org.osmdroid.wms.v111.Layer;
import org.osmdroid.wms.v111.OnlineResource;
import org.osmdroid.wms.v111.Post;
import org.osmdroid.wms.v111.Request;
import org.osmdroid.wms.v111.SRS;
import org.osmdroid.wms.v111.Service;
import org.osmdroid.wms.v111.Style;
import org.osmdroid.wms.v111.WMTMSCapabilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
 * @since 5.6.6
 */

public class DomParserWms111 {
    public static WMSEndpoint parse(Element element) throws Exception {
        WMTMSCapabilities ret = new WMTMSCapabilities();


        WMSEndpoint rets = new WMSEndpoint();
        rets.setWmsVersion(element.getAttribute("version"));
        //check the version attribute
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            if (("Service").equals(e.getNodeName())) {
                ret.setService(parseService(e));

            } else if (("Capability").equals(e.getNodeName())) {
                ret.setCapability(parseCapability(e));

            }
        }
        /*if ("1.3.0".equals(rets.getWmsVersion()) && ret.getService()!=null &&
                ret.getService().getOnlineResource()!=null &&
                ret.getService().getOnlineResource().getXlinkHref()!=null) {
            rets.setBaseurl(ret.getService().getOnlineResource().getXlinkHref());
        } else*/
        try {
            //TODO null checks
            //look for gets
            boolean found = false;
            for (int i = 0; i < ret.getCapability().getRequest().getGetCapabilities().getDCPType().size(); i++) {
                for (int k = 0; k < ret.getCapability().getRequest().getGetCapabilities().getDCPType().get(i).getHTTP().getGetOrPost().size(); k++) {
                    Object or = ret.getCapability().getRequest().getGetCapabilities().getDCPType().get(i).getHTTP().getGetOrPost().get(k);
                    if (or instanceof Get) {
                        rets.setBaseurl(((Get) or).getOnlineResource().getXlinkHref());
                        found = true;
                        break;

                    }
                }
            }

            //rets.baseurl = or.getXlinkHref();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < ret.getCapability().getLayer().getLayer().size(); i++) {
            //if (ret.getCapability().getLayer().getLayer().get(i).getSRS().contains("EPSG:3857"))
            {
                //ok we can handle it here

                WMSLayer l = new WMSLayer();
                String srsStr = "EPSG:900913";
                for (SRS srs : ret.getCapability().getLayer().getLayer().get(i).getSRS()) {
                    if ("EPSG:3857".equals(srs.getvalue())) {
                        srsStr = srs.getvalue();
                        break;
                    }
                }
                l.setSrs(srsStr);
                l.setName(ret.getCapability().getLayer().getLayer().get(i).getName());
                l.setDescription(ret.getCapability().getLayer().getLayer().get(i).getAbstract());
                l.setTitle(ret.getCapability().getLayer().getLayer().get(i).getTitle());
                l.setBbox(ret.getCapability().getLayer().getLayer().get(i).getLatLonBoundingBox());
                if (l.getBbox() == null)
                    l.setBbox(ret.getCapability().getLayer().getLatLonBoundingBox());
                for (Style style : ret.getCapability().getLayer().getLayer().get(i).getStyle()){
                    l.getStyles().add(style.getName());
                }
                rets.getLayers().add(l);

            }
        }

        return rets;
    }


    private static Capability parseCapability(Node element) throws Exception {

        Capability ret = new Capability();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            //System.out.println("parseCapabilties/" + name);
            // Starts by looking for the entry tag
            if (name.equals("Request")) {
                ret.setRequest(parseRequest(e));

            } else if (name.equals("Exception")) {


            } else if (name.equals("Layer")) {
                ret.setLayer(parseLayer(e));

            } else {

            }
        }
        return ret;
    }

    private static Request parseRequest(Node element) throws Exception {
        Request ret = new Request();

        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            // Starts by looking for the entry tag
            //GetMap
            //GetFeatureInfo
            //DescribeLayer
            //GetLegendGraphic
            //GetStyles
            if (name.equals("GetCapabilities")) {
                ret.setGetCapabilities(parseGetCapabilities(e));

            }

        }


        return ret;
    }

    private static GetCapabilities parseGetCapabilities(Node element) throws Exception {
        GetCapabilities ret = new GetCapabilities();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();

            // Starts by looking for the entry tag
            if (name.equals("Format")) {
                Format f = new Format();

                f.setvalue(e.getTextContent());

                ret.getFormat().add(f);

            } else if (name.equals("DCPType")) {
                DCPType type = parseDCPType(e);
                if (type != null)
                    ret.getDCPType().add(type);
            }

        }


        return ret;
    }

    private static DCPType parseDCPType(Node element) throws Exception {

        DCPType ret = new DCPType();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();

            // Starts by looking for the entry tag
            if (name.equals("HTTP")) {
                ret.setHTTP(parserHttp(e));


            }
        }


        return ret;
    }

    private static HTTP parserHttp(Node element) throws Exception {

        HTTP ret = new HTTP();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();

            // Starts by looking for the entry tag
            if (name.equals("Get")) {
                Get get = parseGet(e);

                ret.getGetOrPost().add(get);


            } else if (name.equals("Post")) {
                Post get = parsePost(e);

                ret.getGetOrPost().add(get);


            }
        }


        return ret;

    }

    private static Get parseGet(Node element) throws Exception {
        Get ret = new Get();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();

            // Starts by looking for the entry tag
            if (name.equals("OnlineResource")) {

                OnlineResource r = new OnlineResource();
                Node href2 = e.getAttributes().getNamedItem("xlink:href");
                Node href = e.getAttributes().getNamedItem("href");
                String val = null;
                if (href != null)
                    val = href.getNodeValue();
                if (href2 != null)
                    val = href2.getNodeValue();


                if (val != null)
                    r.setXlinkHref(val);


                if (r.getXlinkHref() != null)
                    ret.setOnlineResource(r);
                //parser.next();

            }

        }


        return ret;
    }

    private static Post parsePost(Node element) throws Exception {
        Post ret = new Post();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();

            // Starts by looking for the entry tag
            if (name.equals("OnlineResource")) {

                OnlineResource r = new OnlineResource();
                Node href2 = e.getAttributes().getNamedItem("xlink:href");
                Node href = e.getAttributes().getNamedItem("href");
                String val = null;
                if (href != null)
                    val = href.getNodeValue();
                if (href2 != null)
                    val = href2.getNodeValue();


                if (val != null)
                    r.setXlinkHref(val);


                if (r.getXlinkHref() != null)
                    ret.setOnlineResource(r);

            }

        }
        return ret;
    }


    private static Layer parseLayer(Node element) throws Exception {

        Layer ret = new Layer();
        Node queryable = element.getAttributes().getNamedItem("queryable");
        if (queryable != null)
            ret.setQueryable(queryable.getNodeValue());
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            if ("Name".equals(name)) {
                ret.setName(e.getTextContent());
            } else if (name.equals("Title")) {
                ret.setTitle(e.getTextContent());
            } else if (name.equals("Abstract")) {
                ret.setAbstract(e.getTextContent());
            } else if (name.equals("SRS")) {
                SRS srs = new SRS();
                srs.setvalue(e.getTextContent());
                ret.getSRS().add(srs);
            } else if (name.equals("CRS")) {
                SRS srs = new SRS();
                srs.setvalue(e.getTextContent());
                ret.getSRS().add(srs);
            } else if (name.equals("LatLonBoundingBox")) {
                ret.setLatLonBoundingBox(new LatLonBoundingBox());
                ret.getLatLonBoundingBox().setMaxx(e.getAttributes().getNamedItem("maxx").getNodeValue());
                ret.getLatLonBoundingBox().setMaxy(e.getAttributes().getNamedItem("maxy").getNodeValue());
                ret.getLatLonBoundingBox().setMiny(e.getAttributes().getNamedItem("miny").getNodeValue());
                ret.getLatLonBoundingBox().setMinx(e.getAttributes().getNamedItem("minx").getNodeValue());
            } else if (name.equals("BoundingBox")) {
                //need to check CRS first if it's valid
                //<BoundingBox CRS="CRS:84" minx="-179.999996" miny="-89.000000" maxx="179.999996" maxy="89.000000"/>
                //<BoundingBox CRS="EPSG:4326" minx="-89.000000" miny="-179.999996" maxx="89.000000" maxy="179.999996"/>
                Node crs = e.getAttributes().getNamedItem("CRS");
                if (crs != null && ret.getLatLonBoundingBox() != null) {
                    if ("EPSG:4326".equals(crs.getNodeValue()) ||
                        "CRS:84".equals(crs.getNodeValue())) {

                        Node maxx = crs.getAttributes().getNamedItem("maxx");
                        Node maxy = crs.getAttributes().getNamedItem("maxy");
                        Node miny = crs.getAttributes().getNamedItem("miny");
                        Node minx = crs.getAttributes().getNamedItem("minx");
                        boolean ok = maxx != null && maxy != null && minx != null && miny != null;
                        if (ok) {
                            ret.setLatLonBoundingBox(new LatLonBoundingBox());
                            ret.getLatLonBoundingBox().setMaxx(maxx.getNodeValue());
                            ret.getLatLonBoundingBox().setMaxy(maxy.getNodeValue());
                            ret.getLatLonBoundingBox().setMiny(miny.getNodeValue());
                            ret.getLatLonBoundingBox().setMinx(minx.getNodeValue());
                        }
                    }
                }
            } else if ("Style".equals(name)) {
                for (int k= 0; k < e.getChildNodes().getLength(); k++) {
                    Node e2 = e.getChildNodes().item(k);
                    if ("Name".equals(e2.getNodeName())){
                        Style style = new Style();
                        style.setName(e2.getTextContent());
                        ret.getStyle().add(style);
                    }
                }


            } else if (name.equals("Layer")) {
                ret.getLayer().add(parseLayer(e));

            } else {

            }
        }
        return ret;
    }


    private static Service parseService(Node element) throws Exception {
        Service ret = new Service();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node e = element.getChildNodes().item(i);
            String name = e.getNodeName();
            // Starts by looking for the entry tag
            if (name.equals("Name")) {
                ret.setName(e.getTextContent());
            } else if (name.equals("Title")) {
                ret.setTitle(e.getTextContent());
            } else if (name.equals("Abstract")) {
                ret.setAbstract(e.getTextContent());
            } else if ("OnlineResource".equals(name)) {
                Node namedItem = e.getAttributes().getNamedItem("xlink:href");
                Node namedItem2 = e.getAttributes().getNamedItem("href");
                String baseUrl = null;
                if (namedItem != null)
                    baseUrl = namedItem.getNodeValue();
                if (namedItem2 != null)
                    baseUrl = namedItem2.getNodeValue();
                if (baseUrl != null) {
                    ret.setOnlineResource(new OnlineResource());
                    ret.getOnlineResource().setXlinkHref(baseUrl);
                }

            }
        }
        return ret;
    }
}

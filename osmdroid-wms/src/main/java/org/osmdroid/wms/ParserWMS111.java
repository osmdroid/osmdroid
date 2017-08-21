package org.osmdroid.wms;

import org.osmdroid.wms.v111.BoundingBox;
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
import org.osmdroid.wms.v111.WMTMSCapabilities;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Because parsing XML documents to a pojo in android is a pin
 * Created by alex on 11/5/15.
 */
public class ParserWMS111 {

    public WMSEndpoint parse(InputStream inputStream) throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);
        parser.nextTag();
        return readFeed(parser);
    }

    private WMSEndpoint readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        WMTMSCapabilities ret = new WMTMSCapabilities();


        WMSEndpoint rets = new WMSEndpoint();


        parser.require(XmlPullParser.START_TAG, null, "WMT_MS_Capabilities");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();//title abstract keyword list onlineResource contactinformation fees accessconstraints
            // Starts by looking for the entry tag
            if (name != null) {
                if (("WMT_MS_Capabilities").equals(name)) {
                    //ret.setVersion(parser.getAttributeValue(null, "version"));
                    //ret.setUpdateSequence(parser.getAttributeValue(null, "updateSequence"));

                } else if (("Service").equals(name)) {
                    ret.setService(parseService(parser));

                } else if (("Capability").equals(name)) {
                    ret.setCapability(parseCapability(parser));

                } else {
                    skip(parser);
                }
            } else {
                skip(parser);
            }
        }

        try {
            //blah blah revised with a million null checks
            //look for gets
            boolean found = false;
            for (int i = 0; i < ret.getCapability().getRequest().getGetCapabilities().getDCPType().size(); i++) {
                for (int k = 0; k < ret.getCapability().getRequest().getGetCapabilities().getDCPType().get(i).getHTTP().getGetOrPost().size(); k++) {
                    Object or = ret.getCapability().getRequest().getGetCapabilities().getDCPType().get(i).getHTTP().getGetOrPost().get(k);
                    if (or instanceof Get) {
                        rets.baseurl = ((Get) or).getOnlineResource().getXlinkHref();
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
                l.name = ret.getCapability().getLayer().getLayer().get(i).getNoSubsets();
                l.description = ret.getCapability().getLayer().getLayer().get(i).getAbstract();
                l.title = ret.getCapability().getLayer().getLayer().get(i).getTitle();
                rets.layers.add(l);

            }
        }

        return rets;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private Capability parseCapability(XmlPullParser parser) throws IOException, XmlPullParserException {

        Capability ret = new Capability();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            System.out.println("parseCapabilties/" + name);
            // Starts by looking for the entry tag
            if (name.equals("Request")) {
                ret.setRequest(parseRequest(parser));

            } else if (name.equals("Exception")) {
                skip(parser);

            } else if (name.equals("Layer")) {
                ret.setLayer(parseLayer(parser));

            } else {
                skip(parser);
            }
        }
        return ret;
    }

    private Request parseRequest(XmlPullParser parser) throws IOException, XmlPullParserException {
        Request ret = new Request();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            //GetMap
            //GetFeatureInfo
            //DescribeLayer
            //GetLegendGraphic
            //GetStyles
            if (name.equals("GetCapabilities")) {
                ret.setGetCapabilities(parseGetCapabilities(parser));
                parser.next();
                parser.next();
            } else
                skip(parser);

        }


        return ret;
    }

    private GetCapabilities parseGetCapabilities(XmlPullParser parser) throws IOException, XmlPullParserException {
        GetCapabilities ret = new GetCapabilities();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("Format")) {
                Format f = new Format();
                parser.next();
                f.setvalue(parser.getText());
                parser.next();
                ret.getFormat().add(f);

            } else if (name.equals("DCPType")) {
                DCPType type = parseDCPType(parser);
                if (type != null)
                    ret.getDCPType().add(type);
            } else
                skip(parser);

        }


        return ret;
    }

    private DCPType parseDCPType(XmlPullParser parser) throws IOException, XmlPullParserException {

        DCPType ret = new DCPType();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("HTTP")) {
                ret.setHTTP(parserHttp(parser));
                ;


            } else
                skip(parser);

        }


        return ret;
    }

    private HTTP parserHttp(XmlPullParser parser) throws IOException, XmlPullParserException {

        HTTP ret = new HTTP();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("Get")) {
                Get get = parseGet(parser);

                ret.getGetOrPost().add(get);


            } else if (name.equals("Post")) {
                Post get = parsePost(parser);

                ret.getGetOrPost().add(get);


            } else
                skip(parser);

        }


        return ret;

    }

    private Get parseGet(XmlPullParser parser) throws IOException, XmlPullParserException {
        Get ret = new Get();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("OnlineResource")) {

                OnlineResource r = new OnlineResource();
                String val = parser.getAttributeValue("xlink", "href");
                String val2 = parser.getAttributeValue("http://www.w3.org/1999/xlink", "href");
                String val3 = parser.getAttributeValue(null, "href");
                String val4 = null;
                int cnt = parser.getAttributeCount();
                for (int i = 0; i < cnt; i++) {
                    String nameattr = parser.getAttributeName(i);
                    if (nameattr.toLowerCase().contains("href")) {
                        val4 = parser.getAttributeValue(i);
                    }
                }
                if (val != null)
                    r.setXlinkHref(val);
                if (val2 != null)
                    r.setXlinkHref(val2);
                if (val3 != null)
                    r.setXlinkHref(val3);
                if (val4 != null)
                    r.setXlinkHref(val4);
                if (r.getXlinkHref() != null)
                    ret.setOnlineResource(r);
                //parser.next();

            } else
                skip(parser);

        }


        return ret;
    }

    private Post parsePost(XmlPullParser parser) throws IOException, XmlPullParserException {
        Post ret = new Post();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("OnlineResource")) {
                OnlineResource r = new OnlineResource();
                String val = parser.getAttributeValue("xlink", "href");
                String val2 = parser.getAttributeValue("http://www.w3.org/1999/xlink", "href");
                if (val != null)
                    r.setXlinkHref(val);
                if (val2 != null)
                    r.setXlinkHref(val2);
                if (r.getXlinkHref() != null)
                    ret.setOnlineResource(r);


            } else
                skip(parser);

        }
        return ret;
    }


    private Layer parseLayer(XmlPullParser parser) throws IOException, XmlPullParserException {

        Layer ret = new Layer();
        ret.setQueryable(parser.getAttributeValue(null, "queryable"));
        while (parser.next() != XmlPullParser.END_TAG) {

            System.out.println("parseLayer/" + parser.getName());

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //System.out.println("parseLayer/" + name);
            if ("Name".equals(name)) {
                parser.next();
               // System.out.println("parseLayer/" + name + "/" + parser.getName());
                ret.setName(parser.getText());
               // System.out.println("parseLayer/" + name + "/" + parser.getName());
               parser.next();
            } else if (name.equals("Title")) {
                parser.next();
               // System.out.println("parseLayer/" + name + "/" + parser.getName());
                ret.setTitle(parser.getText());
                parser.next();
                //System.out.println("parseLayer/" + name + "/" + parser.getName());

            } else if (name.equals("Abstract")) {
                parser.next();
              //  System.out.println("parseLayer/" + name + "/" + parser.getName());
                ret.setAbstract(parser.getText());
                parser.next();
              //  System.out.println("parseLayer/" + name + "/" + parser.getName());

            } else if (name.equals("SRS")) {
                // ret.getSRS().add(parseLayer(parser));
                SRS srs = new SRS();
                parser.next();
                srs.setvalue(parser.getText());
                parser.next();
                ret.getSRS().add(srs);

            } else if (name.equals("LatLonBoundingBox")) {
                ret.setLatLonBoundingBox(new LatLonBoundingBox());
                ret.getLatLonBoundingBox().setMaxx(parser.getAttributeValue(null, "maxx"));
                ret.getLatLonBoundingBox().setMaxy(parser.getAttributeValue(null, "maxy"));
                ret.getLatLonBoundingBox().setMiny(parser.getAttributeValue(null, "miny"));
                ret.getLatLonBoundingBox().setMinx(parser.getAttributeValue(null, "minx"));
                parser.next();

            } else if (name.equals("BoundingBox")) {
                /*ret.getBoundingBox().add(parseBoundingBox(parser));
                ret.getLatLonBoundingBox().setMaxx(parser.getAttributeValue(null, "maxx"));
                ret.getLatLonBoundingBox().setMaxy(parser.getAttributeValue(null, "maxy"));
                ret.getLatLonBoundingBox().setMiny(parser.getAttributeValue(null, "miny"));
                ret.getLatLonBoundingBox().setMinx(parser.getAttributeValue(null, "minx"));*/
                parser.next();

            } else if (name.equals("Layer")) {
                ret.getLayer().add(parseLayer(parser));

            } else {
                skip(parser);
            }
        }
        return ret;
    }

    private BoundingBox parseBoundingBox(XmlPullParser parser) {
        return null;
    }

    private Layer parseLayerElement(XmlPullParser parser) {

        return null;
    }

    private Service parseService(XmlPullParser parser) throws IOException, XmlPullParserException {
        Service ret = new Service();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Name")) {
                parser.next();
                ret.setName(parser.getText());
                parser.next();

            } else if (name.equals("Title")) {
                parser.next();
                ret.setTitle(parser.getText());
                parser.next();

            } else if (name.equals("Abstract")) {
                parser.next();
                ret.setAbstract(parser.getText());
                parser.next();

            } else {
                //parser.next();
                skip(parser);
            }
        }
        return ret;
    }
}

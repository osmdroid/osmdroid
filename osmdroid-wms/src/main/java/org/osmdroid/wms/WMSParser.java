package org.osmdroid.wms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This is the main entry point for working with WMS servers.
 * Sample code<br>
 * <pre>
 * HtpURLConnection c = null;
 * InputStream is = null;
 * WMSEndpoint endpoint = null;
 * try {
 * c = (HttpURLConnection) new URL(youEditTextValue).openConnection();
 * is = c.getInputStream();
 * endpoint = WMSParser.parse(is);
 * } catch (Exception ex) {
 * ex.printStackTrace();
 * } finally {
 * if (is != null)
 * try { is.close(); } catch (Exception ex) { }
 * if (c != null)
 * try { c.disconnect(); } catch (Exception ex) { }
 * }
 *     </pre>
 * created on 8/25/2017.
 * https://github.com/osmdroid/osmdroid/issues/177
 * <p>
 * See also the sample usage in the "Open Map" demo
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class WMSParser {

    /**
     * note, the input stream remains open after calling this method, closing it is the caller's problem
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static WMSEndpoint parse(InputStream inputStream) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new StringReader(""));

            }
        });
        Document doc = dBuilder.parse(inputStream);


        Element element = doc.getDocumentElement();
        element.normalize();

        if (element.getNodeName().contains("WMT_MS_Capabilities")) {
            return DomParserWms111.parse(element);
        } else if (element.getNodeName().contains("WMS_Capabilities")) {
            return DomParserWms111.parse(element);
        }
        throw new IllegalArgumentException("Unknown root element: " + element.getNodeName());


    }

}

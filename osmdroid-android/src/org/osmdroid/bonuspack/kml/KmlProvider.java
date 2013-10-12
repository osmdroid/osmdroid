package org.osmdroid.bonuspack.kml;

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class KmlProvider {

	/**
	 * @param uri of the KML content. Can be for instance Google Map the url of a KML layer created with Google Maps. 
	 * @return DOM Document. 
	 */
	public static Document getKml(String uri){
		DocumentBuilder docBuilder;
		Document kmlDoc = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			kmlDoc = docBuilder.parse(uri);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return kmlDoc;
	}

	protected static GeoPoint parseGeoPoint(String input){
		String[] coords = input.split(",");
		double lon = Double.parseDouble(coords[0]);
		double lat = Double.parseDouble(coords[1]);
		double alt = (coords.length == 3 ? Double.parseDouble(coords[2]) : 0.0);
		GeoPoint p = new GeoPoint(lat, lon, alt);
		return p;
	}
	
	protected static ArrayList<GeoPoint> getCoordinates(Element node){
		String coords = getChildText(node);
		String[] splitted = coords.split(" ");
		ArrayList<GeoPoint> list = new ArrayList<GeoPoint>(splitted.length);
		for (int i=0; i<splitted.length; i++){
			GeoPoint p = parseGeoPoint(splitted[i]);
			list.add(p);
		}
		return list;
	}

	protected static final int NO_SHAPE=0, POINT=1, LINE_STRING=2;

	public static String getChildText(Element element) {
		StringBuilder builder = new StringBuilder();
		NodeList list = element.getChildNodes();
		for(int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if(type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
				builder.append(node.getNodeValue());
			}
		}
		return builder.toString();
	}
	
	/**
	 * From a KML document, fill overlay with placemarks
	 * @param kmlDoc
	 * @param placemarksOverlay
	 * @param context
	 * @param marker
	 */
	public static void buildOverlays(Document kmlDoc, ItemizedOverlayWithBubble<ExtendedOverlayItem> placemarksOverlay, 
			Context context, Drawable marker){
		Element kmlRoot = kmlDoc.getDocumentElement();
		NodeList placemarks = kmlRoot.getElementsByTagName("Placemark");
		for (int p=0; p<placemarks.getLength(); p++){
			Element placemark = (Element)placemarks.item(p);
			NodeList components = placemark.getElementsByTagName("*");
			String name = "", description = "";
			int type =  NO_SHAPE;
			ArrayList<GeoPoint> list = null;
			for (int c=0; c<components.getLength(); c++){
				Element component = (Element)components.item(c);
				String nodeName = component.getTagName();
				if ("name".equals(nodeName)){
					name = getChildText(component);
				} else if ("description".equals(nodeName)){
						description = getChildText(component);
				} else if ("Point".equals(nodeName)){
					NodeList coordinates = placemark.getElementsByTagName("coordinates");
					if (coordinates.getLength()>0){
						type = POINT;
						list = getCoordinates((Element)coordinates.item(0));
					}
				} else if ("LineString".equals(nodeName)){
					NodeList coordinates = placemark.getElementsByTagName("coordinates");
					if (coordinates.getLength()>0){
						type = LINE_STRING;
						list = getCoordinates((Element)coordinates.item(0));
					}
				}
			} //for placemark components
			
			//Fill the overlay with the placemark:
			switch (type){
			case POINT:
				ExtendedOverlayItem item = new ExtendedOverlayItem(name, description, list.get(0), context);
				item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
				item.setMarker(marker);
				placemarksOverlay.addItem(item);
				break;
			case LINE_STRING:
				//TODO
				break;
			default:
				break;
			}
		} //for all placemarks
	}
}
